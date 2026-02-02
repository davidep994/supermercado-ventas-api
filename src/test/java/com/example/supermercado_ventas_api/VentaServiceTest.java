package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.dtos.DetalleRequestDTO;
import com.example.supermercado_ventas_api.dtos.VentaRequestDTO;
import com.example.supermercado_ventas_api.dtos.VentaResponseDTO;
import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.models.Venta;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import com.example.supermercado_ventas_api.services.VentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)

class VentaServiceTest {
    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private SucursalRepository sucursalRepository;
    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    void testRegistrarVenta() {
        Long sucursalId = 1L;
        Long productoId = 1L;

        Sucursal sucursalMock = Sucursal.builder().id(sucursalId).nombreSucursal("Sucursal 1").build();
        Producto productoMock = Producto.builder().id(productoId).nombreProducto("Producto 1").precioProducto(BigDecimal.TEN).build();

        Inventario inventarioMock = Inventario.builder().id(1L).sucursal(sucursalMock).producto(productoMock).cantidad(10).build();
        VentaRequestDTO RequestDTO = new VentaRequestDTO(sucursalId, List.of(new DetalleRequestDTO(productoId, 1)));

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursalMock));
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoMock));
        when(inventarioRepository.findBySucursalAndProducto(sucursalMock, productoMock)).thenReturn(Optional.of(inventarioMock));

        when(ventaRepository.save(any(Venta.class))).thenAnswer(i -> {
            Venta venta = i.getArgument(0);
            venta.setId(1L);
            return venta;

        });

        VentaResponseDTO resultado = ventaService.registrarVenta(RequestDTO);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("10.0"), resultado.total());
        assertEquals(9, inventarioMock.getCantidad());

        verify(ventaRepository, times(1)).save(any(Venta.class));
        verify(inventarioRepository, times(1)).save(inventarioMock);


    }

    @Test
    void testRegistrarVentaSinStock() {
        Long sucursalId = 1L;
        Long productoId = 1L;

        Sucursal sucursalMock = Sucursal.builder().id(sucursalId).nombreSucursal("Sucursal 1").build();
        Producto productoMock = Producto.builder().id(productoId).nombreProducto("Producto 1").precioProducto(BigDecimal.TEN).build();

        Inventario inventarioMock = Inventario.builder().id(1L).sucursal(sucursalMock).producto(productoMock).cantidad(1).build();
        VentaRequestDTO requestDTO = new VentaRequestDTO(sucursalId, List.of(new DetalleRequestDTO(productoId, 5)));

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursalMock));
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoMock));
        when(inventarioRepository.findBySucursalAndProducto(sucursalMock, productoMock)).thenReturn(Optional.of(inventarioMock));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> ventaService.registrarVenta(requestDTO));
        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(ventaRepository, never()).save(any());
    }
}
