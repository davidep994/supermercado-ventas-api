package com.example.supermercado_ventas_api.repositories;

import com.example.supermercado_ventas_api.dtos.VentaResponseDTO;
import com.example.supermercado_ventas_api.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta,Long> {
    List<Venta> findByFiltrosAvanzados(@Param("idSucursal") Long idSucursal,@Param("fecha") LocalDate fecha,@Param("soloActivas") boolean soloActivas);
}
