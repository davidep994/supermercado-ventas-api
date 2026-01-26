package com.example.supermercado_ventas_api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombreProducto;

    @NotNull(message = "El precio es obligatorio.")
    @Positive(message = "El precio debe ser mayor que 0.")
    @Column(nullable = false)
    private BigDecimal precioProducto;

    @NotBlank
    @Column(nullable = false)
    private String categoria;

}
