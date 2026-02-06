package com.example.supermercado_ventas_api.dtos;

import java.math.BigDecimal;

/**
 * DTO de respuesta que representa el detalle de un producto
 * dentro de una venta registrada.
 */
public record DetalleVentaResponseDTO(
        Long idProducto,
        String nombreProducto,
        String categoria,
        Integer cantidad,
        BigDecimal precioUnidad,
        BigDecimal subtotal
) {
}
