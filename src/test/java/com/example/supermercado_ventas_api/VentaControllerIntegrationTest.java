package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.dtos.DetalleRequestDTO;
import com.example.supermercado_ventas_api.dtos.VentaRequestDTO;
import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
 class VentaControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InventarioRepository inventarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private SucursalRepository sucursalRepository;

    @Test
    @WithMockUser(username = "admin")
    void testRegistrarVenta() throws Exception {
        Sucursal sucursal = sucursalRepository.save(Sucursal.builder()
                .nombreSucursal("Sucursal 1")
                .direccion("Av. Principal 123")
                .build());

        Producto producto = productoRepository.save(Producto.builder()
                .nombreProducto("Arroz Premium")
                .precioProducto(BigDecimal.valueOf(2.50))
                .categoria("Alimentos")
                .build());

        inventarioRepository.save(Inventario.builder()
                .sucursal(sucursal)
                .producto(producto)
                .cantidad(10)
                .build());

        VentaRequestDTO requestDTO = new VentaRequestDTO(
                sucursal.getId(),
                List.of(new DetalleRequestDTO(producto.getId(), 2)));

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(5.00))
                .andExpect(jsonPath("$.nombreSucursal").value("Sucursal 1"));

    }

}
