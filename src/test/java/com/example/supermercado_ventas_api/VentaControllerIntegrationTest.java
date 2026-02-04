package com.example.supermercado_ventas_api;

import com.example.supermercado_ventas_api.dtos.DetalleRequestDTO;
import com.example.supermercado_ventas_api.dtos.VentaRequestDTO;
import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.models.Venta;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VentaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper; // Para serializar/deserializar JSON

    // Repositorios para la preparación de escenarios (Arrange) y validaciones (Assert)
    @Autowired
    private InventarioRepository inventarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private SucursalRepository sucursalRepository;
    @Autowired
    private VentaRepository ventaRepository;

    private Sucursal sucursalDefault;
    private Producto productoDefault;

    /**
     * Configuración inicial ejecutada antes de cada test.
     * Establece los datos maestros mínimos necesarios (Sucursal y Producto).
     */
    @BeforeEach
    void setup() {
        sucursalDefault = sucursalRepository.save(Sucursal.builder()
                .nombreSucursal("Sucursal Test")
                .direccion("Calle 123")
                .build());

        productoDefault = productoRepository.save(Producto.builder()
                .nombreProducto("Producto Test")
                .precioProducto(BigDecimal.valueOf(1.50))
                .categoria("Categoria")
                .build());
    }

    /**
     * Verifica que una venta se registre correctamente cuando los datos son válidos
     * y existe suficiente stock. Debe retornar HTTP 201 Created.
     */
    @Test
    @DisplayName("POST /api/ventas - Debería registrar venta exitosamente")
    @WithMockUser(username = "cajero")
    void testRegistrarVentaExitoso() throws Exception {
        // Arrange: Preparar inventario con stock suficiente (20 unidades)
        crearInventario(sucursalDefault, productoDefault, 20);

        VentaRequestDTO requestDTO = new VentaRequestDTO(
                sucursalDefault.getId(),
                List.of(new DetalleRequestDTO(productoDefault.getId(), 5)));

        // Act & Assert: Ejecutar petición POST y validar respuesta JSON
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(7.50)) // 5 * 1.50
                .andExpect(jsonPath("$.nombreSucursal").value("Sucursal Test"));
    }

    /**
     * Verifica la regla de negocio de stock insuficiente.
     * El sistema debe impedir la venta y retornar HTTP 409 Conflict.
     */
    @Test
    @DisplayName("POST /api/ventas - Debería fallar si no hay stock suficiente")
    @WithMockUser(username = "cajero")
    void testRegistrarVentaSinStock() throws Exception{
        // Arrange: Establecer stock crítico (2 unidades)
        crearInventario(sucursalDefault, productoDefault, 2);

        // Crear DTO solicitando cantidad superior al stock (10 unidades)
        VentaRequestDTO requestDTO = new VentaRequestDTO(
                sucursalDefault.getId(),
                List.of(new DetalleRequestDTO(productoDefault.getId(), 10))
        );

        // Act & Assert: Validar que se lance la excepción de negocio mapeada a 409
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    /**
     * Verifica las validaciones de entrada (@Valid).
     * Una lista de productos vacía debe resultar en HTTP 400 Bad Request.
     */
    @Test
    @DisplayName("POST /api/ventas - Debería validar datos de entrada incorrectos")
    @WithMockUser(username = "admin")
    void testRegistrarVentaValidacionFallida() throws Exception {
        // Arrange: DTO con lista de detalles vacía
        VentaRequestDTO requestDTO = new VentaRequestDTO(sucursalDefault.getId(), List.of());

        // Act & Assert: Validar respuesta de error de cliente
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifica la capacidad de filtrado del endpoint GET.
     * Debe retornar solo las ventas asociadas a la sucursal específica.
     */
    @Test
    @DisplayName("GET /api/ventas - Debería filtrar ventas por sucursal")
    @WithMockUser(username = "admin")
    void testBuscarVentasPorSucursal() throws Exception {
        // Arrange: Persistir una venta válida en base de datos
        crearInventario(sucursalDefault, productoDefault, 10);

        VentaRequestDTO ventaRequest = new VentaRequestDTO(sucursalDefault.getId(),
                List.of(new DetalleRequestDTO(productoDefault.getId(), 2)));

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ventaRequest)));

        // Act: Consultar ventas filtrando por la sucursal creada
        // Assert: Validar estructura del array de respuesta y cálculo de totales
        mockMvc.perform(get("/api/ventas")
                        .param("idSucursal", String.valueOf(sucursalDefault.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].total").value(3.00));
    }

    /**
     * Verifica el borrado lógico de una venta.
     * La entidad no debe eliminarse físicamente, sino cambiar su estado a inactivo.
     */
    @Test
    @DisplayName("DELETE /api/ventas/{id} - Anular venta (Borrado Lógico)")
    @WithMockUser(username = "admin")
    void testAnularVenta() throws Exception {
        // Arrange: Crear escenario y registrar una venta inicial
        crearInventario(sucursalDefault, productoDefault, 10);

        // Nota: Usamos ArrayList explícito para garantizar mutabilidad si el servicio lo requiere
        VentaRequestDTO requestCrear = new VentaRequestDTO(
                sucursalDefault.getId(),
                new ArrayList<>(List.of(new DetalleRequestDTO(productoDefault.getId(), 2)))
        );

        String responseContent = mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCrear)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseContent);
        Long ventaId = jsonNode.get("id").asLong();

        // Act: Ejecutar anulación (DELETE)
        mockMvc.perform(delete("/api/ventas/" + ventaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mensaje").value("Venta anulada con éxito"));

        // Assert (Verificación BD): Recuperar entidad y confirmar cambio de estado
        Venta ventaEnDb = ventaRepository.findById(ventaId).orElseThrow();
        assertFalse(ventaEnDb.getActiva(), "La venta debe estar inactiva en BD");
    }

    // --- Métodos Auxiliares ---
    private void crearInventario(Sucursal s, Producto p, Integer cantidad) {
        inventarioRepository.save(Inventario.builder()
                .sucursal(s)
                .producto(p)
                .cantidad(cantidad)
                .build());
    }
}
