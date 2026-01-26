package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.dtos.InventarioRequestDTO;
import com.example.supermercado_ventas_api.dtos.InventarioResponseDTO;
import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.services.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
@Tag(name = "Inventarios", description = "Gestión de inventario de productos.")
public class InventarioController {
    private final InventarioService inventarioService;

    @GetMapping
    @Operation(summary = "Listar inventarios", description = "Obtiene todos los inventarios disponibles.")
    public ResponseEntity<List<InventarioResponseDTO>> verStock(@RequestParam(required = false) Long sucursalId, @RequestParam(required = false) Long productoId) {
        List<InventarioResponseDTO> stock = inventarioService.verStock(sucursalId, productoId);
        return ResponseEntity.ok(stock);
    }

    @PostMapping("/agregar")
    @Operation(summary = "Agregar inventario", description = "Agrega un nuevo inventario.")
    public ResponseEntity<?> agregarInventario(@Valid @RequestBody InventarioRequestDTO inventarioDTO) {
        Inventario inventario = inventarioService.agregarInventario(inventarioDTO);
        String mensaje = String.format("✅ Se han añadido %d unidades de '%s' a la '%s'.",
                inventarioDTO.cantidad(),
                inventario.getProducto().getNombreProducto(),
                inventario.getSucursal().getNombreSucursal());
        return ResponseEntity.ok(Map.of("message", mensaje, "producto", inventario.getProducto().getNombreProducto(), "sucursal", inventario.getSucursal().getNombreSucursal(), "nuevo stock total", inventario.getCantidad()));
    }


}
