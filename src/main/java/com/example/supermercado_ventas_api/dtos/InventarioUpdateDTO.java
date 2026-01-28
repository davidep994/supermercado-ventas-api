package com.example.supermercado_ventas_api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventarioUpdateDTO(
        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 0, message = "El stock no puede ser negativo.")
        Integer cantidad
) {
}
