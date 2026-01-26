package com.example.supermercado_ventas_api.services;

import com.example.supermercado_ventas_api.dtos.InventarioRequestDTO;
import com.example.supermercado_ventas_api.dtos.InventarioResponseDTO;
import com.example.supermercado_ventas_api.exceptions.ProductoNotFoundException;
import com.example.supermercado_ventas_api.exceptions.SucursalNotFoundException;
import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    public List<InventarioResponseDTO> verStock(Long sucursalId, Long productoId) {
        List<Inventario> resultados;
        if (sucursalId != null && productoId != null) {
            resultados = inventarioRepository.findBySucursalId(sucursalId).stream().filter(i -> i.getProducto().getId().equals(productoId)).toList();

        } else if (sucursalId != null) {
            resultados = inventarioRepository.findBySucursalId(sucursalId);
        } else if (productoId != null) {
            resultados = inventarioRepository.findByProductoId(productoId);
        } else {
            resultados = inventarioRepository.findAll();
        }
        return resultados.stream().map(inv -> new InventarioResponseDTO(inv.getSucursal().getNombreSucursal(), inv.getProducto().getNombreProducto(), inv.getCantidad())).toList();
    }

    public Inventario agregarInventario(InventarioRequestDTO inventarioDTO) {
        Sucursal sucursal = sucursalRepository.findById(inventarioDTO.idSucursal()).orElseThrow(() -> new SucursalNotFoundException(inventarioDTO.idSucursal()));
        Producto producto = productoRepository.findById(inventarioDTO.idProducto()).orElseThrow(() -> new ProductoNotFoundException(inventarioDTO.idProducto()));
        Inventario inventarioExistente = inventarioRepository.findBySucursalAndProducto(sucursal, producto).orElse(Inventario.builder().sucursal(sucursal).producto(producto).cantidad(0).build());
        inventarioExistente.setCantidad(inventarioExistente.getCantidad() + inventarioDTO.cantidad());
        return inventarioRepository.save(inventarioExistente);
    }

}

