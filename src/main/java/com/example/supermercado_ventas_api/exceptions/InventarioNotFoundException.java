package com.example.supermercado_ventas_api.exceptions;

public class InventarioNotFoundException extends ResourceNotFoundException {
    public InventarioNotFoundException(Long id) {
        super("El inventario con ID " + id + " no existe en la base de datos.");
    }
}
