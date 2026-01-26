package com.example.supermercado_ventas_api.repositories;

import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findBySucursalAndProducto(Sucursal sucursal, Producto producto);

    List<Inventario> findBySucursalId(Long sucursalId);

    List<Inventario> findByProductoId(Long productoId);
}
