package com.example.supermercado_ventas_api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(nullable = false)
    private Double totalVenta;
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @OneToMany(mappedBy = "venta")
    private List<Producto> detalleVenta = new ArrayList<>();



}
