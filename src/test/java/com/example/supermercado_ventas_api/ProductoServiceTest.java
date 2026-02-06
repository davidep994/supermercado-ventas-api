package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import com.example.supermercado_ventas_api.services.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Tests unitarios del servicio de productos
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private VentaRepository ventaRepository;
    @Mock
    InventarioRepository inventarioRepository;


    @InjectMocks
    private ProductoService productoService;

    @Test
    @DisplayName("Debe encontrar un producto por ID correctamente")
    void testFindByIdExitoso() {
        Long id = 1L;
        Producto productoMock = Producto.builder()
                .id(id)
                .nombreProducto("Leche")
                .precioProducto(BigDecimal.valueOf(2.00))
                .build();

        when(productoRepository.findById(id)).thenReturn(Optional.of(productoMock));

        Producto resultado = productoService.findById(id);

        // Protección básica: el servicio debe delegar correctamente al repositorio
        assertNotNull(resultado);
        assertEquals("Leche", resultado.getNombreProducto());
        verify(productoRepository, times(1)).findById(id);

    }

    @Test
    @DisplayName("Debe lanzar excepción si el producto que queremos borrar tiene alguna venta activa")
    void testDeleteFallaPorVentas() {
        Long id = 1L;

        when(productoRepository.existsById(id)).thenReturn(true);
        when(ventaRepository.existsByDetalles_Producto_Id(id)).thenReturn(true);


        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productoService.delete(id);
        });

        assertTrue(exception.getMessage().contains("No se puede eliminar el producto porque ya tiene ventas asociados."));

        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe borrar el producto si no tiene ventas ni stock")
    void testDeleteExitoso() {
        Long id = 1L;

        when(productoRepository.existsById(id)).thenReturn(true);
        when(ventaRepository.existsByDetalles_Producto_Id(id)).thenReturn(false);
        when(inventarioRepository.existsByProducto_Id(id)).thenReturn(false);

        productoService.delete(id);

        verify(productoRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debe lanzar excecpión si el producto tiene stock en inventario")
    void testDeleteFallaPorInventario() {
        Long id = 1L;

        when(productoRepository.existsById(id)).thenReturn(true);
        when(ventaRepository.existsByDetalles_Producto_Id(id)).thenReturn(false);
        //Simulamos que si que hay stock
        when(inventarioRepository.existsByProducto_Id(id)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productoService.delete(id);
        });

        assertTrue(exception.getMessage().contains("tiene stock registrado en el inventario"));
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe actualizar los datos de un producto correctamente")
    void testUpdateProducto() {
        Long id = 1L;
        Producto existente = Producto.builder()
                .id(id)
                .nombreProducto("Viejo")
                .precioProducto(BigDecimal.ONE).build();
        Producto nuevosDatos = Producto.builder()
                .nombreProducto("Nuevo")
                .precioProducto(BigDecimal.TEN).build();

        when(productoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto resultado = productoService.update(id, nuevosDatos);

        assertEquals("Nuevo", resultado.getNombreProducto());
        assertEquals(BigDecimal.TEN, resultado.getPrecioProducto());
        verify(productoRepository).save(existente);

    }
}
