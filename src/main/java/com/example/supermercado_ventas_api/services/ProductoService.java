package com.example.supermercado_ventas_api.services;

import com.example.supermercado_ventas_api.exceptions.ProductoNotFoundException;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new ProductoNotFoundException(id));
    }

    @Transactional
    public Producto create(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto update(Long id, Producto producto) {
        Producto productoExistente = findById(id);
        productoExistente.setNombreProducto(producto.getNombreProducto());
        productoExistente.setPrecioProducto(producto.getPrecioProducto());
        productoExistente.setCategoria(producto.getCategoria());
        return productoRepository.save(productoExistente);
    }

    @Transactional
    public void delete(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductoNotFoundException(id);
        }

        // VALIDACIÓN DE SEGURIDAD
        if (ventaRepository.existsByDetalles_Producto_Id(id)) {
            throw new IllegalStateException("No se puede eliminar el producto porque ya tiene ventas asociados.");
        }

        productoRepository.deleteById(id);
    }
}
