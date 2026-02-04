package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.dtos.InventarioRequestDTO;
import com.example.supermercado_ventas_api.exceptions.ProductoNotFoundException;
import com.example.supermercado_ventas_api.exceptions.SucursalNotFoundException;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.supermercado_ventas_api.dtos.InventarioResponseDTO;
import com.example.supermercado_ventas_api.dtos.InventarioUpdateDTO;
import com.example.supermercado_ventas_api.exceptions.ResourceNotFoundException;
import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.services.InventarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class InventarioServiceTest {
    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Test
    @DisplayName("Debe actualizar el stock correctamente")
    void actualizarStock_Exito() {
        Long idInventario = 100L;
        InventarioUpdateDTO dto = new InventarioUpdateDTO(50); // Queremos poner 50 unidades

        Sucursal sucursal = new Sucursal(1L, "Norte", "Calle 1");
        Producto producto = new Producto(1L, "Pan", BigDecimal.ONE, "Comida");

        Inventario inventarioExistente = new Inventario(idInventario, sucursal, producto, 10);

        when(inventarioRepository.findById(idInventario)).thenReturn(Optional.of(inventarioExistente));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioExistente);

        InventarioResponseDTO resultado = inventarioService.actualizarStock(idInventario, dto);

        assertNotNull(resultado);
        assertEquals(50, resultado.getCantidad()); // Verificamos que devuelve 50
        assertEquals(50, inventarioExistente.getCantidad()); // Verificamos que la entidad cambió
        verify(inventarioRepository).save(inventarioExistente);
    }

    @Test
    @DisplayName("Lanza error si el inventario no existe")
    void actualizarStock_NoExiste() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventarioService.actualizarStock(99L, new InventarioUpdateDTO(5)));
    }

    @Test
    @DisplayName("Debe lanzar SucursalNotFoundException si la sucursal no existe")
    void testAgregarInventarioSucursalNoEncontrada() {
        Long sucursalId = 99L; //Id de sucursal que no existe
        Long productoId = 1L;
        InventarioRequestDTO requestDTO = new InventarioRequestDTO(sucursalId, productoId, 10);

        //Simulación de que el repositorio de sucursales no devuelve nada
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.empty());

        //Verificamos que lanza la excepción correcta
        assertThrows(SucursalNotFoundException.class, () -> {
            inventarioService.agregarInventario(requestDTO);
        });

        //Verificamos que nunca se llamó al repositorio de productos ni al repositorio de inventario
        verify(productoRepository, never()).findById(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe lanzar ProductoNotFoundException cuando el producto no exista")
    void testAgregarInventarioProductoNoEncontrado() {
        Long sucursalId = 1L;
        Long productoId = 88L; //Id de producto que no existe
        InventarioRequestDTO requestDTO = new InventarioRequestDTO(sucursalId, productoId, 10);
        Sucursal sucursalMock = Sucursal.builder().id(sucursalId).build();

        //Verificación sucursal: si existe
        when(sucursalRepository.findById(sucursalId)).thenReturn(Optional.of(sucursalMock));

        //Verificación producto: no existe
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> {
            inventarioService.agregarInventario(requestDTO);
        });

        //Verificamos que se quedó en el segundo paso y nunca intentó buscar en el inventario ni guardar
        verify(inventarioRepository, never()).findBySucursalAndProducto(any(), any());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debe eliminar el inventario si el ID existe")
    void testEliminarInventarioExitoso() {
        Long inventarioId = 1L;
        //Simulamos que el ID existe en la BBDD
        when(inventarioRepository.existsById(inventarioId)).thenReturn(true);

        inventarioService.eliminarInventario(inventarioId);

        //Verificamos que se llamó al metodo deleteById una vez
        verify(inventarioRepository, times(1)).deleteById(inventarioId);
    }

    @Test
    @DisplayName("Debe lanzar InventarioNotFoundException al intentar eliminar un inventario")
    void testEliminarInventarioNoEncontrado() {
        Long inventarioId = 1L;
        when(inventarioRepository.existsById(inventarioId)).thenReturn(false);

        assertThrows(com.example.supermercado_ventas_api.exceptions.InventarioNotFoundException.class, () -> {
            inventarioService.eliminarInventario(inventarioId);
        });

        //Verificamos que nunca se intentó borrar nada
        verify(inventarioRepository, never()).deleteById(anyLong());
    }

}
