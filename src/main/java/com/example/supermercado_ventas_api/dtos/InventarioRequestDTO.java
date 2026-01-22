package com.example.supermercado_ventas_api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventarioRequestDTO(
        @NotNull(message = "El ID de la sucursal es obligatorio")
        Long idSucursal,
        @NotNull(message = "El ID del producto es obligatorio")
        Long idProducto,
        @NotNull(message = "La cantidad es obligatorio")
        @Min(value = 1, message = "La cantidad mínima es 1")
        Integer cantidad
) {
}
