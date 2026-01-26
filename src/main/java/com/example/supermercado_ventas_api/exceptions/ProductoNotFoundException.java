package com.example.supermercado_ventas_api.exceptions;

public class ProductoNotFoundException extends ResourceNotFoundException {

    public ProductoNotFoundException(Long id) {
        super("El producto con ID " + id + " no existe en el inventario");
    }
}
