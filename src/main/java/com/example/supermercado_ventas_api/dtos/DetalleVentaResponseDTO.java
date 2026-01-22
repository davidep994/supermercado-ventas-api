package com.example.supermercado_ventas_api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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
