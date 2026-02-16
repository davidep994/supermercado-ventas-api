package com.example.supermercado_ventas_api.configs;

import com.example.supermercado_ventas_api.models.Usuario;
import com.example.supermercado_ventas_api.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final UsuarioRepository usuarioRepository; // Inyectamos el repo para obtener el rol real

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Omitir filtros para Swagger y documentación
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtils.validateJwtToken(token)) {
                String username = jwtUtils.getUsernameFromJwtToken(token);

                // Buscamos al usuario en la BD para conocer su ROL
                usuarioRepository.findByUsername(username).ifPresent(usuario -> {
                    // Convertimos el Enum Rol a una autoridad reconocida por Spring Security
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(usuario.getRol().name());

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(authority) // Aquí pasamos el ROL real (ADMIN o CAJERO)
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}