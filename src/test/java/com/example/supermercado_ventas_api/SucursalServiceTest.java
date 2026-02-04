package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.exceptions.SucursalNotFoundException;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import com.example.supermercado_ventas_api.services.SucursalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private SucursalService sucursalService;

    @Test
    @DisplayName("Debe listar todas las sucursales correctamente")
    void testFindAll() {
        // Preparar datos mock
        Sucursal s1 = Sucursal.builder().id(1L).nombreSucursal("Centro").direccion("Av. Principal 123").build();
        Sucursal s2 = Sucursal.builder().id(2L).nombreSucursal("Norte").direccion("Calle Industrial 55").build();

        when(sucursalRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        // Ejecutar
        List<Sucursal> resultado = sucursalService.findAll();

        // Verificar
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(sucursalRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe encontrar una sucursal por ID")
    void testFindByIdExitoso() {
        Long id = 1L;
        Sucursal sucursal = Sucursal.builder()
                .id(id)
                .nombreSucursal("Centro")
                .direccion("Av. Principal 123")
                .build();

        when(sucursalRepository.findById(id)).thenReturn(Optional.of(sucursal));

        // Ejecutar
        Sucursal resultado = sucursalService.findById(id);

        // Verificar
        assertNotNull(resultado);
        assertEquals("Centro", resultado.getNombreSucursal());
        assertEquals("Av. Principal 123", resultado.getDireccion());
        verify(sucursalRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar excepción si la sucursal no existe")
    void testFindByIdNoEncontrado() {
        Long id = 99L;
        when(sucursalRepository.findById(id)).thenReturn(Optional.empty());

        // Verificar que lanza la excepción
        assertThrows(SucursalNotFoundException.class, () -> sucursalService.findById(id));
        verify(sucursalRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe crear una nueva sucursal correctamente")
    void testCreateExitoso() {
        Sucursal nuevaSucursal = Sucursal.builder()
                .nombreSucursal("Sur")
                .direccion("Calle Sur 789")
                .build();

        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(nuevaSucursal);

        // Ejecutar
        Sucursal resultado = sucursalService.create(nuevaSucursal);

        // Verificar resultado
        assertNotNull(resultado);
        assertEquals("Sur", resultado.getNombreSucursal());
        verify(sucursalRepository, times(1)).save(nuevaSucursal);
    }

    @Test
    @DisplayName("Debe actualizar una sucursal existente")
    void testUpdateExitoso() {
        Long id = 1L;
        Sucursal existente = Sucursal.builder()
                .id(id)
                .nombreSucursal("Viejo")
                .direccion("Dirección Vieja")
                .build();

        Sucursal nuevosDatos = Sucursal.builder()
                .nombreSucursal("Nuevo")
                .direccion("Dirección Nueva")
                .build();

        when(sucursalRepository.findById(id)).thenReturn(Optional.of(existente));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(i -> i.getArgument(0));

        // Ejecutar
        Sucursal resultado = sucursalService.update(id, nuevosDatos);

        // Verificar que se actualizaron los campos
        assertEquals("Nuevo", resultado.getNombreSucursal());
        assertEquals("Dirección Nueva", resultado.getDireccion());
        verify(sucursalRepository).save(existente);
    }

    @Test
    @DisplayName("Debe eliminar una sucursal si no tiene ventas asociadas")
    void testDeleteExitoso() {
        Long id = 1L;

        when(sucursalRepository.existsById(id)).thenReturn(true);
        when(ventaRepository.existsBySucursal_Id(id)).thenReturn(false);

        // Ejecutar
        sucursalService.delete(id);

        // Verificar que se eliminó
        verify(sucursalRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar sucursal con ventas")
    void testDeleteFallaPorVentas() {
        Long id = 1L;

        when(sucursalRepository.existsById(id)).thenReturn(true);
        when(ventaRepository.existsBySucursal_Id(id)).thenReturn(true);

        // Verificar que lanza excepción
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> sucursalService.delete(id));

        assertTrue(exception.getMessage().contains("tiene ventas asociadas"));
        verify(sucursalRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar sucursal inexistente")
    void testDeleteSucursalNoExiste() {
        Long id = 99L;
        when(sucursalRepository.existsById(id)).thenReturn(false);

        // Verificar que lanza excepción
        assertThrows(SucursalNotFoundException.class, () -> sucursalService.delete(id));
        verify(sucursalRepository, never()).deleteById(anyLong());
    }
}