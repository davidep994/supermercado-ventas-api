package com.example.supermercado_ventas_api.dtos;

public record ProductoTopVentasDTO(
        String nombre,
        Long totalVendido
) {
}
