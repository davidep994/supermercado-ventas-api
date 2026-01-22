package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.dtos.VentaRequestDTO;
import com.example.supermercado_ventas_api.dtos.VentaResponseDTO;
import com.example.supermercado_ventas_api.models.Venta;
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

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas")
public class VentaController {
    private final VentaService ventaService;
    @PostMapping
    @Operation(summary = "Registrar una venta", description = "Registra una venta asociada a una sucursal y productos")
    public ResponseEntity<VentaResponseDTO> registrarVenta(@Valid @RequestBody VentaRequestDTO ventaDTO){
        return new ResponseEntity<>(ventaService.registrarVenta(ventaDTO), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar ventas", description = "Filtra por sucursal, fecha y estado (Activas/Todas)")
    public ResponseEntity<List<VentaResponseDTO>> buscarVenta(@RequestParam(required = false) Long idSucursal,
                                                              @RequestParam(required = false)LocalDate fecha,
                                                              @RequestParam(required = false, defaultValue = "false")boolean soloActivas){
        return ResponseEntity.ok(ventaService.buscarVentas(idSucursal, fecha, soloActivas));

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Anular venta", description = "Realiza un borrado lógico de la venta")
    public ResponseEntity<Map<String, String>> borrarVenta(@PathVariable Long id){
        ventaService.borrarVentaLogica(id);
        return ResponseEntity.ok(Map.of("Mensaje", "Venta anulada con éxito"));
    }



}
