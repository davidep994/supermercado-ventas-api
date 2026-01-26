package com.example.supermercado_ventas_api.repositories;

import com.example.supermercado_ventas_api.dtos.ProductoTopVentasDTO;
import com.example.supermercado_ventas_api.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByDetalles_Producto_Id(Long productoId);

    boolean existsBySucursal_Id(Long idSucursal);

    @Query("SELECT v FROM Venta v JOIN FETCH v.sucursal WHERE " +
            "(:soloActivas = false OR v.activa = true) AND " +
            "(:idSucursal IS NULL OR v.sucursal.id = :idSucursal) AND " +
            "(:fecha IS NULL OR CAST(v.fecha AS LocalDate) = :fecha)")
    List<Venta> findByFiltrosAvanzados(@Param("idSucursal") Long idSucursal,
                                       @Param("fecha") LocalDate fecha,
                                       @Param("soloActivas") boolean soloActivas);

    @Query("SELECT new com.example.supermercado_ventas_api.dtos.ProductoTopVentasDTO(d.producto.nombreProducto, SUM(d.cantidad)) " +
            "FROM VentaDetalle d " +
            "GROUP BY d.producto.id, d.producto.nombreProducto " +
            "ORDER BY SUM(d.cantidad) DESC")
    List<ProductoTopVentasDTO> findProductoMasVendido(Pageable pageable);
}
