package com.example.supermercado_ventas_api.services;

import com.example.supermercado_ventas_api.dtos.*;
import com.example.supermercado_ventas_api.exceptions.ProductoNotFoundException;
import com.example.supermercado_ventas_api.exceptions.SucursalNotFoundException;
import com.example.supermercado_ventas_api.exceptions.VentaNotFoundException;
import com.example.supermercado_ventas_api.models.*;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final InventarioRepository inventarioRepository;

    @Transactional
    public VentaResponseDTO registrarVenta(VentaRequestDTO ventaDTO) {
        // 1. Validar existencia de la sucursal
        Sucursal sucursal = sucursalRepository.findById(ventaDTO.idSucursal())
                .orElseThrow(() -> new SucursalNotFoundException(ventaDTO.idSucursal()));

        // 2. Cargar productos en un mapa para evitar múltiples consultas a la BD
        List<Long> productoIDs = ventaDTO.detalle().stream()
                .map(DetalleRequestDTO::idProducto)
                .toList();

        Map<Long, Producto> productosMap = productoRepository.findAllById(productoIDs).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        // --- FASE DE VALIDACIÓN PREVIA ---
        // Verificamos stock de TODO el carrito antes de realizar cualquier descuento
        for (DetalleRequestDTO item : ventaDTO.detalle()) {
            Producto producto = productosMap.get(item.idProducto());
            if (producto == null) {
                throw new ProductoNotFoundException(item.idProducto());
            }

            Inventario inventario = inventarioRepository.findBySucursalAndProducto(sucursal, producto)
                    .orElseThrow(() -> new IllegalStateException("El producto '" + producto.getNombreProducto() + "' no está registrado en esta sucursal."));

            if (inventario.getCantidad() < item.cantidad()) {
                // Mensaje detallado para el Frontend
                throw new IllegalStateException(String.format(
                        "Stock insuficiente para '%s'. Disponible: %d, solicitado: %d",
                        producto.getNombreProducto(), inventario.getCantidad(), item.cantidad()
                ));
            }
        }

        // --- FASE DE PROCESAMIENTO ---
        Venta venta = new Venta();
        venta.setSucursal(sucursal);
        venta.setFecha(LocalDateTime.now());
        venta.setActiva(true);

        List<VentaDetalle> detalles = ventaDTO.detalle().stream()
                .map(item -> {
                    Producto producto = productosMap.get(item.idProducto());
                    Inventario inventario = inventarioRepository.findBySucursalAndProducto(sucursal, producto).get();

                    // Actualizar y guardar inventario
                    inventario.setCantidad(inventario.getCantidad() - item.cantidad());
                    inventarioRepository.save(inventario);

                    return VentaDetalle.builder()
                            .venta(venta)
                            .producto(producto)
                            .cantidad(item.cantidad())
                            .build();
                }).toList();

        venta.setDetalles(new ArrayList<>(detalles));

        // Calcular el total de la venta
        BigDecimal totalVenta = venta.getDetalles().stream()
                .map(d -> d.getProducto().getPrecioProducto().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        venta.setTotalVenta(totalVenta);

        // Guardar la venta y retornar el DTO
        Venta ventaGuardada = ventaRepository.save(venta);
        return mapToDTO(ventaGuardada);
    }

    public List<VentaResponseDTO> buscarVentas(Long idSucursal, LocalDate fecha, boolean soloActivas) {
        if (idSucursal != null && !sucursalRepository.existsById(idSucursal)) {
            throw new SucursalNotFoundException(idSucursal);
        }
        return ventaRepository.findByFiltrosAvanzados(idSucursal, fecha, soloActivas).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public void borrarVentaLogica(Long id) {
        Venta venta = ventaRepository.findById(id).orElseThrow(() -> new VentaNotFoundException(id));

        if (!venta.getActiva()) {
            throw new IllegalStateException("Esta venta ya fue anulada anteriormente");
        }
        for (VentaDetalle ventaDetalle : venta.getDetalles()) {
            Inventario inventario = inventarioRepository.findBySucursalAndProducto(venta.getSucursal(), ventaDetalle.getProducto())
                    .orElseThrow(() -> new IllegalStateException("Error de integridad: no se encuentra inventario para reponer stock"));
            inventario.setCantidad(inventario.getCantidad() + ventaDetalle.getCantidad());
            inventarioRepository.save(inventario);
        }
        venta.setActiva(false);
        ventaRepository.save(venta);
    }

    private VentaResponseDTO mapToDTO(Venta v) {
        String nombreSucursal = (v.getSucursal() != null) ? v.getSucursal().getNombreSucursal() : "Sin sucursal";
        List<DetalleVentaResponseDTO> detallesDTO = v.getDetalles().stream()
                .map(ventaDetalle -> {
                    Producto p = ventaDetalle.getProducto();
                    BigDecimal subtotal = p.getPrecioProducto().multiply(BigDecimal.valueOf(ventaDetalle.getCantidad()));
                    return new DetalleVentaResponseDTO(
                            p.getId(),
                            p.getNombreProducto(),
                            p.getCategoria(),
                            ventaDetalle.getCantidad(),
                            p.getPrecioProducto(),
                            subtotal
                    );
                }).toList();
        return new VentaResponseDTO(
                v.getId(),
                nombreSucursal,
                v.getFecha(),
                v.getTotalVenta(),
                v.getActiva(),
                detallesDTO
        );
    }

    public ProductoTopVentasDTO obtenerProductoMasVendido() {
        List<ProductoTopVentasDTO> result = ventaRepository.findProductoMasVendido(PageRequest.of(0, 1));

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    public List<Map<String, Object>> obtenerEstadisticasVentas() {
        return ventaRepository.findVentasDiarias().stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", obj[0].toString().substring(0, 10)); // Solo la fecha YYYY-MM-DD
            map.put("ventas", obj[1]);
            return map;
        }).collect(Collectors.toList());
    }
}
