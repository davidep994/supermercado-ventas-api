package com.example.supermercado_ventas_api.dtos;

//DTO de respuesta que representa el producto con mayor volumen de ventas
public record ProductoTopVentasDTO(
        String nombre,
        Long totalVendido
) {
}
