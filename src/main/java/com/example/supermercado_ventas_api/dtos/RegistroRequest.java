package com.example.supermercado_ventas_api.dtos;

import com.example.supermercado_ventas_api.models.Rol;

public record RegistroRequest(String username, String password, Rol rol) {}
