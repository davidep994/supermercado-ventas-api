package com.example.supermercado_ventas_api.exceptions;
/**
 * Excepción base utilizada cuando un recurso solicitado
 * no existe en el sistema.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
