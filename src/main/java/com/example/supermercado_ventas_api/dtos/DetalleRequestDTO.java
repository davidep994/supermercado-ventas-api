package com.example.supermercado_ventas_api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
/**
 * DTO utilizado para representar un detalle de venta,
 * indicando el producto y la cantidad solicitada.
 */
public record DetalleRequestDTO(
        @NotNull
        Long idProducto,
        @Min(value = 1, message = "La cantidad mínima es 1.")
        Integer cantidad
) {
}
