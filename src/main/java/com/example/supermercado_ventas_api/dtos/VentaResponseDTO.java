package com.example.supermercado_ventas_api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta que representa una venta registrada,
 * incluyendo su información general y el detalle de productos vendidos.
 */
public record VentaResponseDTO(
        Long id,
        String nombreSucursal,
        LocalDateTime fecha,
        BigDecimal total,
        boolean active,
        List<DetalleVentaResponseDTO> detalles) {

}
