package com.example.supermercado_ventas_api.controllers;

import com.example.supermercado_ventas_api.configs.JWTUtils;
import com.example.supermercado_ventas_api.models.Rol;
import com.example.supermercado_ventas_api.models.Usuario;
import com.example.supermercado_ventas_api.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final JWTUtils jwtUtils;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest request) {
        if (usuarioRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
        }

        Usuario nuevoUsuario = Usuario.builder()
                .username(request.username())
                // AQUÍ ENCRIPTAMOS:
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .build();

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado como " + request.rol()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return usuarioRepository.findByUsername(request.username())
                // AQUÍ COMPARAMOS DE FORMA SEGURA:
                .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtils.generateToken(user.getUsername());
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "rol", user.getRol(),
                            "username", user.getUsername()
                    ));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas")));
    }

    public record LoginRequest(String username, String password) {}
    public record RegistroRequest(String username, String password, Rol rol) {}
}