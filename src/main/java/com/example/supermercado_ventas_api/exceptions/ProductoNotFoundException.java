package com.example.supermercado_ventas_api.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un producto
 * con el identificador indicado.
 */
public class ProductoNotFoundException extends ResourceNotFoundException {

    public ProductoNotFoundException(Long id) {
        super("El producto con ID " + id + " no existe en el inventario");
    }
}
