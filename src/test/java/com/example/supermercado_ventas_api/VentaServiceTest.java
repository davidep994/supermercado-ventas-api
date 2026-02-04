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
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.anyList;
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
    @DisplayName("Debe registrar una venta exitosamente si hay stock")
    void testRegistrarVenta() {
        Long sucursalId = 1L;
        Long productoId = 1L;

        Sucursal sucursalMock = Sucursal.builder().id(sucursalId).nombreSucursal("Sucursal 1").build();
        Producto productoMock = Producto.builder().id(productoId).nombreProducto("Producto 1").precioProducto(BigDecimal.TEN).build();

        Inventario inventarioMock = Inventario.builder()
                .id(1L)
                .sucursal(sucursalMock)
                .producto(productoMock)
                .cantidad(10)
                .build();

        VentaRequestDTO requestDTO = new VentaRequestDTO(
                sucursalId,
                List.of(new DetalleRequestDTO(productoId, 2)));

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursalMock));
        when(productoRepository.findAllById(anyList())).thenReturn(List.of(productoMock));
        when(inventarioRepository.findBySucursalAndProducto(sucursalMock, productoMock)).thenReturn(Optional.of(inventarioMock));

        when(ventaRepository.save(any(Venta.class))).thenAnswer(i -> {
            Venta venta = i.getArgument(0);
            venta.setId(1L);
            return venta;
        });

        VentaResponseDTO resultado = ventaService.registrarVenta(requestDTO);

        assertNotNull(resultado);
        assertEquals(0, new BigDecimal("20.0").compareTo(resultado.total()), "El total debe ser 20.0");
        assertEquals(8, inventarioMock.getCantidad(), "El stock debe quedar en 8");

        verify(ventaRepository, times(1)).save(any(Venta.class));
        verify(inventarioRepository, times(1)).save(inventarioMock);
    }

    @Test
    @DisplayName("Debe lanzar error si no hay stock suficiente")
    void testRegistrarVentaSinStock() {
        Long sucursalId = 1L;
        Long productoId = 1L;

        Sucursal sucursalMock = Sucursal.builder().id(sucursalId).nombreSucursal("Sucursal 1").build();
        Producto productoMock = Producto.builder().id(productoId).nombreProducto("Producto 1").precioProducto(BigDecimal.TEN).build();

        Inventario inventarioMock = Inventario.builder().id(1L).sucursal(sucursalMock).producto(productoMock).cantidad(1).build();
        VentaRequestDTO requestDTO = new VentaRequestDTO(sucursalId, List.of(new DetalleRequestDTO(productoId, 5)));

        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursalMock));
        when(productoRepository.findAllById(anyList())).thenReturn(List.of(productoMock));
        when(inventarioRepository.findBySucursalAndProducto(sucursalMock, productoMock)).thenReturn(Optional.of(inventarioMock));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> ventaService.registrarVenta(requestDTO));

        assertTrue(exception.getMessage().contains("Stock insuficiente"));

        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar error si el producto no existe en el inventario de la sucursal especifica")
    void testRegistrarVentaNoExisteInventario() {
        //Preparación
        Long sucursalId = 1L;
        Long productoId = 1L;
        Sucursal sucursalMock = Sucursal.builder()
                .id(sucursalId)
                .build();
        Producto productoMock = Producto.builder()
                .id(productoId)
                .nombreProducto("Pan")
                .build();

        //Creamos la solicitud de venta para un producto existente
        VentaRequestDTO request =  new VentaRequestDTO(sucursalId, List.of(new DetalleRequestDTO(productoId, 1)));

        //Simulamos que la suscursal y el producto existen
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursalMock));
        when(productoRepository.findAllById(anyList())).thenReturn(List.of(productoMock));
        //Simulamos que el producto no tiene registro de inventario en esa sucursal
        when(inventarioRepository.findBySucursalAndProducto(sucursalMock, productoMock)).thenReturn(Optional.empty());

        // Verificamos y lanzamos IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> ventaService.registrarVenta(request));

        //Validamos que el mensaje de error sea el esperado para el usuario
        assertTrue(exception.getMessage().contains("no está registrado en el inventario"));

        //Verificamos que nunca se llamó al save del repositorio
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe anular una venta y reponer el stock al inventario")
    void testAnularVentaYReponerStock() {
        Long ventaId = 1L;
        Sucursal sucursal = Sucursal.builder()
                .id(1L)
                .build();
        Producto producto = Producto.builder()
                .id(1L)
                .nombreProducto("Leche")
                .build();

        com.example.supermercado_ventas_api.models.VentaDetalle detalle =
                com.example.supermercado_ventas_api.models.VentaDetalle.builder()
                        .producto(producto)
                        .cantidad(5)
                        .build();

        Venta ventaMock = Venta.builder()
                .id(ventaId).
                activa(true)
                .sucursal(sucursal)
                .detalles(List.of(detalle))
                .build();

        Inventario inventarioMock = Inventario.builder()
                .sucursal(sucursal)
                .producto(producto)
                .cantidad(10)
                .build();

        when(ventaRepository.findById(ventaId)).thenReturn(Optional.of(ventaMock));
        when(inventarioRepository.findBySucursalAndProducto(sucursal, producto)).thenReturn(Optional.of(inventarioMock));

        ventaService.borrarVentaLogica(ventaId);

        assertFalse(ventaMock.getActiva(), "La venta debe estar anulada (activa = false)");
        assertEquals(15, inventarioMock.getCantidad(), "El stock debe haber subido de 10 a 15");

        verify(inventarioRepository).save(inventarioMock);
        verify(ventaRepository).save(ventaMock);
    }



}