package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debe retornar lista de productos")
    void testGetAllProductos() throws Exception {
        //Preparación de los datos
        Producto p1 = Producto.builder()
                .nombreProducto("Leche")
                .precioProducto(BigDecimal.TEN)
                .categoria("Lácteos")
                .build();
        productoRepository.save(p1);

        //Ejecutamos la petición
        mockMvc.perform(get("/api/productos"))
                //Verificación de los resultados
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.nombreProducto == 'Leche')].id").exists());
    }

    @Test
    @DisplayName("Debe crear un producto y persistirlo en la BBDD")
    void testCrearProductoIntegracion() throws Exception {
        //Preparación
        Producto nuevoProducto = Producto.builder()
                .nombreProducto("Leche")
                .precioProducto(new BigDecimal("1.20"))
                .categoria("Lácteos")
                .build();

        //Realizamos una petición POST y validamos la respuesta
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombreProducto").value("Leche"));

        //Verificamos la persistencia real
        Producto guardado = productoRepository.findAll().stream()
                .filter(p -> p.getNombreProducto()
                        .equals("Leche"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("La leche no se encontró en la base de datos"));

        assertEquals("Leche", guardado.getNombreProducto());
    }


}
