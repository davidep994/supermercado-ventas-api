package com.example.supermercado_ventas_api;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.example.supermercado_ventas_api.dtos.InventarioUpdateDTO;
import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventarioControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Repositorios para crear el escenario inicial
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioRepository inventarioRepository;

    @Test
    @WithMockUser(username = "admin")
    void actualizarInventario_Integracion() throws Exception {
        // 1. PREPARAR ESCENARIO (Datos previos en BD H2)
        //TODO Cambiar a MySQL
        Sucursal sucursal = sucursalRepository.save(Sucursal.builder().nombreSucursal("S1").direccion("D1").build());
        Producto producto = productoRepository.save(Producto.builder().nombreProducto("P1").precioProducto(BigDecimal.TEN).categoria("C1").build());

        Inventario inventario = inventarioRepository.save(Inventario.builder()
                .sucursal(sucursal)
                .producto(producto)
                .cantidad(10) // Empezamos con 10
                .build());

        // 2. PETICIÓN: Actualizar a 500 unidades
        InventarioUpdateDTO updateDTO = new InventarioUpdateDTO(500);

        mockMvc.perform(put("/api/inventarios/" + inventario.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(500)) // Verificamos respuesta JSON
                .andExpect(jsonPath("$.nombreProducto").value("P1"));
    }
}
