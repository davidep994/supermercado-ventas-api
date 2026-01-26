package com.example.supermercado_ventas_api.exceptions;

public class VentaNotFoundException extends ResourceNotFoundException {
    public VentaNotFoundException(Long id) {
        super("No se encontró la venta con ID: " + id);
    }
}
