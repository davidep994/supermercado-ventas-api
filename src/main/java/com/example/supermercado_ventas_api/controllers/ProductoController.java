package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * Controlador REST encargado de la gestión de productos del sistema.
 * Expone operaciones CRUD para administrar el catálogo de productos.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de inventario de productos.")
public class ProductoController {
    private final ProductoService productoService;

    // Devuelve la lista completa de productos registrados
    @GetMapping
    @Operation(summary = "Listar productos", description = "Obtiene todos los productos disponibles.")
    public ResponseEntity<List<Producto>> findAll() {
        return ResponseEntity.ok(productoService.findAll());
    }

    // Obtiene un producto concreto a partir de su identificador
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto", description = "Obtiene un producto por su ID.")
    public ResponseEntity<Producto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.findById(id));
    }
    // Crea un nuevo producto en el sistema
    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el inventario.")
    public ResponseEntity<Producto> create(@Valid @RequestBody Producto producto) {
        return new ResponseEntity<>(productoService.create(producto), HttpStatus.CREATED);
    }
    // Actualiza los datos de un producto existente en el sistema
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente.")
    public ResponseEntity<Producto> update(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.update(id, producto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto del inventario")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.ok(Map.of("Mensaje", "Producto eliminado con éxito"));
    }
}
