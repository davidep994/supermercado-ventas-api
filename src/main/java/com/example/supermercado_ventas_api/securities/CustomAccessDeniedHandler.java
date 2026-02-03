package com.example.supermercado_ventas_api.securities;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper(); // Para convertir el Map a JSON

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Devuelve 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", LocalDateTime.now().toString());
        map.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        map.put("error", "No autorizado");
        map.put("message", "Acceso denegado: Token inválido, expirado o ausente.");
        map.put("path", request.getServletPath());

        // Escribe el JSON en la respuesta
        objectMapper.writeValue(response.getOutputStream(), map);
    }
}
