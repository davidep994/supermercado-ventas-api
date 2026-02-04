package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.models.Sucursal;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SucursalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crearSucursal_ok() throws Exception {
        Sucursal sucursal = Sucursal.builder()
                .nombreSucursal("Central")
                .direccion("Calle 1")
                .build();

        mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sucursal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Central"))
                .andExpect(jsonPath("$.direccion").value("Calle 1"));
    }

    @Test
    void obtenerTodasLasSucursales_ok() throws Exception {
        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void obtenerSucursalPorId_ok() throws Exception {
        Sucursal sucursal = Sucursal.builder()
                .nombreSucursal("Norte")
                .direccion("Calle Norte")
                .build();

        String response = mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sucursal)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Sucursal creada = objectMapper.readValue(response, Sucursal.class);

        mockMvc.perform(get("/api/sucursales/{id}", creada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Norte"));
    }

    @Test
    void actualizarSucursal_ok() throws Exception {
        Sucursal sucursal = Sucursal.builder()
                .nombreSucursal("Vieja")
                .direccion("Calle Vieja")
                .build();

        String response = mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sucursal)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Sucursal creada = objectMapper.readValue(response, Sucursal.class);

        Sucursal actualizada = Sucursal.builder()
                .nombreSucursal("Nueva")
                .direccion("Calle Nueva")
                .build();

        mockMvc.perform(put("/api/sucursales/{id}", creada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nueva"))
                .andExpect(jsonPath("$.direccion").value("Calle Nueva"));
    }

    @Test
    void eliminarSucursal_ok() throws Exception {
        Sucursal sucursal = Sucursal.builder()
                .nombreSucursal("Eliminar")
                .direccion("Calle X")
                .build();

        String response = mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sucursal)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Sucursal creada = objectMapper.readValue(response, Sucursal.class);

        mockMvc.perform(delete("/api/sucursales/{id}", creada.getId()))
                .andExpect(status().isOk());
    }
}
