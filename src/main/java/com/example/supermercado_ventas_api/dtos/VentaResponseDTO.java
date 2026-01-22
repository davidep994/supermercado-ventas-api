package com.example.supermercado_ventas_api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponseDTO(
        Long id,
        String nombreSucursal,
        LocalDateTime fecha,
        BigDecimal total,
        boolean active,
        List<DetalleVentaResponseDTO> detalles) {

}
