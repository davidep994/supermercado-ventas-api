package com.example.supermercado_ventas_api.services;

import com.example.supermercado_ventas_api.dtos.InventarioRequestDTO;
import com.example.supermercado_ventas_api.dtos.InventarioResponseDTO;
import com.example.supermercado_ventas_api.dtos.InventarioUpdateDTO;
import com.example.supermercado_ventas_api.exceptions.InventarioNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    // 1. Ver Stock del inventario
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

        return resultados.stream()
                .map(this::mapToRespondeDTO)
                .toList();
    }

    // 2. Agregar Stock al inventario
    public InventarioResponseDTO agregarInventario(InventarioRequestDTO inventarioDTO) {
        Sucursal sucursal = sucursalRepository.findById(inventarioDTO.idSucursal())
                .orElseThrow(() -> new SucursalNotFoundException(inventarioDTO.idSucursal()));

        Producto producto = productoRepository.findById(inventarioDTO.idProducto())
                .orElseThrow(() -> new ProductoNotFoundException(inventarioDTO.idProducto()));

        Inventario inventarioExistente = inventarioRepository.findBySucursalAndProducto(sucursal, producto)
                .orElse(Inventario.builder()
                        .sucursal(sucursal)
                        .producto(producto)
                        .cantidad(0)
                        .build());

        inventarioExistente.setCantidad(inventarioExistente.getCantidad() + inventarioDTO.cantidad());
        inventarioRepository.save(inventarioExistente);

        return mapToRespondeDTO(inventarioExistente);
    }

    // 3. Modificar Stock de un inventario
    @Transactional
    public InventarioResponseDTO actualizarStock(Long id, InventarioUpdateDTO inventarioDTO) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new InventarioNotFoundException(id));

        inventario.setCantidad(inventarioDTO.cantidad());

        Inventario nuevoInventario = inventarioRepository.save(inventario);
        return mapToRespondeDTO(nuevoInventario);
    }

    // 4. Eliminar el Stock de un inventario
    @Transactional
    public void eliminarInventario(Long id) {
        if (!inventarioRepository.existsById(id)){
            throw new InventarioNotFoundException(id);
        }
        inventarioRepository.deleteById(id);
    }

    // Mapper Auxiliar
    private InventarioResponseDTO mapToRespondeDTO(Inventario inventario) {
        return InventarioResponseDTO.builder()
                .id(inventario.getId())
                .idSucursal(inventario.getSucursal().getId())
                .nombreSucursal(inventario.getSucursal().getNombreSucursal())
                .idProducto(inventario.getProducto().getId())
                .nombreProducto(inventario.getProducto().getNombreProducto())
                .precioUnitario(inventario.getProducto().getPrecioProducto())
                .cantidad(inventario.getCantidad())
                .build();
    }
}

