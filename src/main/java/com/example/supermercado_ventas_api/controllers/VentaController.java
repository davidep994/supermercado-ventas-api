package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.dtos.VentaRequestDTO;
import com.example.supermercado_ventas_api.dtos.VentaResponseDTO;
import com.example.supermercado_ventas_api.exceptions.ResourceNotFoundException;
import com.example.supermercado_ventas_api.services.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
/**
 * Controlador REST encargado de la gestión de ventas del sistema.
 * Permite registrar, consultar y anular ventas.
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas")
public class VentaController {
    private final VentaService ventaService;

//Registra una nueva venta asociada a una sucursal y a uno o varios productos
    @PostMapping
    @Operation(summary = "Registrar una venta", description = "Registra una venta asociada a una sucursal y productos")
    public ResponseEntity<VentaResponseDTO> registrarVenta(@Valid @RequestBody VentaRequestDTO ventaDTO) {
        return new ResponseEntity<>(ventaService.registrarVenta(ventaDTO), HttpStatus.CREATED);
    }
    /**
      Permite buscar ventas aplicando filtros opcionales por sucursal,
      fecha y estado de la venta */
    @GetMapping
    @Operation(summary = "Buscar ventas", description = "Filtra por sucursal, fecha y estado (Activas/Todas)")
    public ResponseEntity<List<VentaResponseDTO>> buscarVenta(@RequestParam(required = false) Long idSucursal,
                                                              @RequestParam(required = false) LocalDate fecha,
                                                              @RequestParam(required = false, defaultValue = "false") boolean soloActivas) {

        List<VentaResponseDTO> resultados = ventaService.buscarVentas(idSucursal, fecha, soloActivas);

        if (resultados.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron ventas con los filtros proporcionados.");
        }

        return ResponseEntity.ok(resultados);
    }

    //Anula una venta mediante borrado lógico
    @DeleteMapping("/{id}")
    @Operation(summary = "Anular venta", description = "Realiza un borrado lógico de la venta")
    public ResponseEntity<Map<String, String>> borrarVenta(@PathVariable Long id) {
        ventaService.borrarVentaLogica(id);
        return ResponseEntity.ok(Map.of("Mensaje", "Venta anulada con éxito"));
    }
}
