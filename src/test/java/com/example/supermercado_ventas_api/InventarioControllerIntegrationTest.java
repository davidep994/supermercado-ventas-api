package com.example.supermercado_ventas_api;

import org.junit.jupiter.api.DisplayName;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventarioControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // Repositorios para crear el escenario inicial
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private InventarioRepository inventarioRepository;

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

    @Test
    @DisplayName("Debe listar el inventario filtrado por sucursal")
    void ListarInventarioIntegration() throws Exception {
        //Creamos datos
        Sucursal s = sucursalRepository.save(Sucursal.builder().nombreSucursal("Sucursal Test").direccion("Dir").build());
        Producto p = productoRepository.save(Producto.builder().nombreProducto("P1").precioProducto(BigDecimal.ONE).categoria("Cat").build());

        inventarioRepository.save(Inventario.builder()
                .sucursal(s)
                .producto(p)
                .cantidad(100)
                .build());

        //Ejecución y validación
        mockMvc.perform(get("/api/inventarios")
                        .param("sucursalId", s.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombreProducto").value("P1"))
                .andExpect(jsonPath("$[0].cantidad").value("100"));
    }

    @Test
    @WithMockUser
    @DisplayName("Debe eliminar un registro de inventario de la base de datos")
    void eliminarInventarioIntegration() throws Exception {
        //Creamos datos
        Sucursal s = sucursalRepository.save(Sucursal.builder().nombreSucursal("S1").direccion("D1").build());
        Producto p = productoRepository.save(Producto.builder().nombreProducto("Borrar").precioProducto(new BigDecimal("10.00")).categoria("C").build());

        Inventario inv = inventarioRepository.save(Inventario.builder()
                .sucursal(s)
                .producto(p)
                .cantidad(10)
                .build());

        //Ejecución
        mockMvc.perform(delete("/api/inventarios/" + inv.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inventario eliminado con éxito."));

        //Verificamos en el repositorio que el ID ya no existe
        boolean existe = inventarioRepository.existsById(inv.getId());
        org.junit.jupiter.api.Assertions.assertFalse(existe, "El registro debería haber desaparecido de la BBDD");
    }

    @Test
    @WithMockUser
    @DisplayName("Debe agregar un nuevo inventario exitosamente")
    void agregarNuevoInventarioIntegration() throws Exception {
        //Preparación
        Sucursal sucursal = sucursalRepository.save(Sucursal.builder().nombreSucursal("S1").direccion("D1").build());
        Producto producto = productoRepository.save(Producto.builder().nombreProducto("P1").precioProducto(new BigDecimal("2.50")).categoria("Cat").build());

        com.example.supermercado_ventas_api.dtos.InventarioRequestDTO requestDTO = new com.example.supermercado_ventas_api.dtos.InventarioRequestDTO(
                sucursal.getId(),
                producto.getId(),
                50);

        //Ejecución
        mockMvc.perform(post("/api/inventarios/agregar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))

                //Validación
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.producto").value("P1"))
                .andExpect(jsonPath("$.sucursal").value("S1"))
                .andExpect(jsonPath("$.['nuevo stock total']").value(50));

        //Verificación de persistencia
        boolean existe = inventarioRepository.findAll().stream()
                .anyMatch(inv -> inv.getProducto().getId().equals(producto.getId()) && inv.getCantidad().equals(50));

        org.junit.jupiter.api.Assertions.assertTrue(existe, "El inventario debería estar persistido en la base de datos");
    }


}
