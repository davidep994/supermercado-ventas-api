package com.example.supermercado_ventas_api.dtos;

import java.math.BigDecimal;

public record DetalleVentaResponseDTO(
        Long idProducto,
        String nombreProducto,
        String categoria,
        Integer cantidad,
        BigDecimal precioUnidad,
        BigDecimal subtotal
) {
}
