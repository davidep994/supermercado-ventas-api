package com.example.supermercado_ventas_api.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaRequestDTO(
        @NotNull(message = "La sucursal es obligatoria.")
        Long idSucursal,
        @NotEmpty(message = "La venta debe tener almenos un producto")
        @Valid
        List<DetalleRequestDTO> detalle) {


}
