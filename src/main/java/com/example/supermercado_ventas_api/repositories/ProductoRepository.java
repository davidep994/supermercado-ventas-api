package com.example.supermercado_ventas_api.repositories;

import com.example.supermercado_ventas_api.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {


}
