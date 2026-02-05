package com.example.supermercado_ventas_api.exceptions;
/**
 * Excepción lanzada cuando no se encuentra una sucursal
 * con el identificador especificado.
 */
public class SucursalNotFoundException extends ResourceNotFoundException {

    public SucursalNotFoundException(Long id) {
        super("La sucursal con ID " + id + " no existe en la base de datos.");
    }
}
