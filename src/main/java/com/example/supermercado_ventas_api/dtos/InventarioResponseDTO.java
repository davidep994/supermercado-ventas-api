package com.example.supermercado_ventas_api.dtos;

public record InventarioResponseDTO(
        String sucursal,
        String producto,
        Integer cantidad
) {
}
