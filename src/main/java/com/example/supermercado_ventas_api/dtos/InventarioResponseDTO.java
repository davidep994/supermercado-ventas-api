package com.example.supermercado_ventas_api.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de respuesta que representa el estado actual del inventario
 * de un producto en una sucursal.
 */
@Data
@Builder
public class InventarioResponseDTO {
    private Long id;
    private Long idSucursal;
    private String nombreSucursal;
    private Long idProducto;
    private String nombreProducto;
    private BigDecimal precioUnitario;
    private Integer cantidad;
}
