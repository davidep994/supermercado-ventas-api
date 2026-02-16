package com.example.supermercado_ventas_api.securities;

import com.example.supermercado_ventas_api.configs.JWTFilter;
import com.example.supermercado_ventas_api.configs.JWTUtils;
import com.example.supermercado_ventas_api.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad actualizada para soportar Roles (ADMIN/CAJERO)
 * y permitir conexión desde el Frontend (CORS).
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtils jwtUtils;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final UsuarioRepository usuarioRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Habilitar CORS con la configuración definida abajo
                .cors(Customizer.withDefaults())
                // 2. Deshabilitar CSRF ya que usamos JWT
                .csrf(AbstractHttpConfigurer::disable)
                // 3. Política de sesión sin estado
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4. Gestión de excepciones personalizadas
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                // 5. Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Acceso público a Documentación y Auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/api/auth/**"
                        ).permitAll()

                        // Consultas (GET) permitidas para cualquier usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // REGLAS DE ROLES:
                        // Solo el ADMIN puede gestionar Inventario y Productos (POST/PUT/DELETE)
                        .requestMatchers("/api/inventarios/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAuthority("ADMIN")

                        // Solo el ADMIN puede gestionar Sucursales
                        .requestMatchers("/api/sucursales/**").hasAuthority("ADMIN")

                        // Las Ventas pueden ser gestionadas por CAJERO y ADMIN
                        .requestMatchers("/api/ventas/**").hasAnyAuthority("ADMIN", "CAJERO")
                        .requestMatchers("/api/estadisticas/**").hasAnyAuthority("ADMIN", "CAJERO")

                        .anyRequest().authenticated()
                )
                // 6. Añadir el filtro JWT antes del filtro de autenticación de Spring
                .addFilterBefore(new JWTFilter(jwtUtils, usuarioRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración de CORS para permitir que React (normalmente en puerto 5173 o 3000)
     * pueda consumir la API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost")); // URL de tu React
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}