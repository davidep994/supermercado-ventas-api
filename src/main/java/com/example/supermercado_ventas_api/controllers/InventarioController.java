package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.dtos.InventarioRequestDTO;
import com.example.supermercado_ventas_api.dtos.InventarioResponseDTO;
import com.example.supermercado_ventas_api.dtos.InventarioUpdateDTO;
import com.example.supermercado_ventas_api.services.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<List<InventarioResponseDTO>> verStock(
            @RequestParam(required = false) Long sucursalId,
            @RequestParam(required = false) Long productoId) {

        List<InventarioResponseDTO> stock = inventarioService.verStock(sucursalId, productoId);
        return ResponseEntity.ok(stock);
    }

    @PostMapping("/agregar")
    @Operation(summary = "Agregar inventario", description = "Agrega un nuevo inventario.")
    public ResponseEntity<?> agregarInventario(@Valid @RequestBody InventarioRequestDTO inventarioDTO) {

        InventarioResponseDTO inventario = inventarioService.agregarInventario(inventarioDTO);

        String mensaje = String.format("✅ Se han añadido %d unidades de '%s' a la '%s'.",
                inventarioDTO.cantidad(),
                inventario.getNombreProducto(),
                inventario.getNombreSucursal());

        return ResponseEntity.ok(Map.of(
                "message", mensaje,
                "producto", inventario.getNombreProducto(),
                "sucursal", inventario.getNombreSucursal(),
                "nuevo stock total", inventario.getCantidad()
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar inventario", description = "Actualiza el stock de un inventario existente.")
    public ResponseEntity<InventarioResponseDTO>  actualizarInventario(
            @PathVariable Long id,
            @Valid @RequestBody InventarioUpdateDTO inventarioDTO){

        InventarioResponseDTO actualizado = inventarioService.actualizarStock(id, inventarioDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar inventario", description = "Eliminar el stock de un inventario existente.")
    public ResponseEntity<Map<String, Object>> eliminarInventario(@PathVariable Long id){
        inventarioService.eliminarInventario(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inventario eliminado con éxito.");
        response.put("id_eliminado", id);

        return ResponseEntity.ok(response);
    }
}
