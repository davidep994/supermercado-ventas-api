package com.example.supermercado_ventas_api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha es obligatoria.")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotNull(message = "El total no puede ser nulo.")
    @PositiveOrZero(message = "El total no puede ser negativo.")
    @Column(nullable = false)
    private BigDecimal totalVenta;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @NotNull(message = "La sucursal es obligatoria.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Builder.Default
    @NotEmpty(message = "La venta debe tener al menos un producto.")
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaDetalle> detalles = new ArrayList<>();
}
