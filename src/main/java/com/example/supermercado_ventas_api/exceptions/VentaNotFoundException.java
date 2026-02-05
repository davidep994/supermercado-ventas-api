package com.example.supermercado_ventas_api.exceptions;
/**
 * Excepción lanzada cuando no se encuentra una venta
 * con el identificador especificado.
 */
public class VentaNotFoundException extends ResourceNotFoundException {
    public VentaNotFoundException(Long id) {
        super("No se encontró la venta con ID: " + id);
    }
}
