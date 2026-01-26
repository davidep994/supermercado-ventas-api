package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.configs.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.username()) && "123".equals(request.password())) {
            String token = jwtUtils.generateToken(request.username());
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }

    // DTO interno para no crear otro archivo
    public record LoginRequest(String username, String password) {
    }
}
