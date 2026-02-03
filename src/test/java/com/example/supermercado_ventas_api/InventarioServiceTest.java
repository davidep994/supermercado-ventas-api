package com.example.supermercado_ventas_api;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class InventarioServiceTest {
    @Mock
    private InventarioRepository inventarioRepository;

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
}
