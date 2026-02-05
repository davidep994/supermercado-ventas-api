package com.example.supermercado_ventas_api.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
/**
 * Configuración de OpenAPI (Swagger) para documentar la API REST
 * e integrar autenticación mediante JWT (Bearer Token).
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Supermarket API", version = "1.0", description = "Documentación para gestión de ventas"),
        security = {@SecurityRequirement(name = "bearerAuth")}
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
