package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.dtos.ProductoTopVentasDTO;
import com.example.supermercado_ventas_api.services.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Tag(name = "Estadísticas", description = "Operaciones de reporte y análisis")
public class EstadisticaController {

    private final VentaService ventaService;

    @GetMapping("/producto-mas-vendido")
    @Operation(summary = "Producto más vendido", description = "Devuelve el objeto del producto top ventas.")
    public ResponseEntity<ProductoTopVentasDTO> getMasVendido() {
        ProductoTopVentasDTO topVenta = ventaService.obtenerProductoMasVendido();

        if (topVenta == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(topVenta);
    }
}
