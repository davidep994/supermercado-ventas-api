package com.example.supermercado_ventas_api.services;

import com.example.supermercado_ventas_api.dtos.DetalleRequestDTO;
import com.example.supermercado_ventas_api.dtos.DetalleVentaResponseDTO;
import com.example.supermercado_ventas_api.dtos.VentaRequestDTO;
import com.example.supermercado_ventas_api.dtos.VentaResponseDTO;
import com.example.supermercado_ventas_api.exceptions.ProductoNotFoundException;
import com.example.supermercado_ventas_api.exceptions.SucursalNotFoundException;
import com.example.supermercado_ventas_api.exceptions.VentaNotFoundException;
import com.example.supermercado_ventas_api.models.*;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        Sucursal sucursal = sucursalRepository.findById(ventaDTO.idSucursal()).orElseThrow(() -> new SucursalNotFoundException(ventaDTO.idSucursal()));
        List<Long> productoIDs = ventaDTO.detalle().stream()
                .map(DetalleRequestDTO::idProducto)
                .toList();
        Map<Long, Producto> productosMap = productoRepository.findAllById(productoIDs).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        Venta venta = new Venta();
        venta.setSucursal(sucursal);
        venta.setFecha(LocalDateTime.now());
        venta.setActivo(true);
        List<VentaDetalle> detalles = ventaDTO.detalle().stream()
                .map(item -> {
                    Producto producto = productosMap.get(item.idProducto());
                    if (producto == null) {
                        throw new ProductoNotFoundException(item.idProducto());
                    }
                    Inventario inventario = inventarioRepository.findBySucursalAndProducto(sucursal, producto)
                            .orElseThrow(()-> new IllegalStateException("El producto '" + producto.getNombreProducto() + "' no está registrado en el inventario de la sucursal " + sucursal.getNombreSucursal()));
                    if (inventario.getCantidad() < item.cantidad()){
                        throw new IllegalStateException("Stock insuficiente para '" + producto.getNombreProducto() + "'. Solicitado: " + item.cantidad() + ", Disponible: " + inventario.getCantidad());
                    }
                    inventario.setCantidad(inventario.getCantidad() - item.cantidad());
                    inventarioRepository.save(inventario);

                    return VentaDetalle.builder()
                            .venta(venta)
                            .producto(producto)
                            .cantidad(item.cantidad())
                            .build();
                }).toList();

        BigDecimal totalVenta = detalles.stream()
                .map(d -> d.getProducto().getPrecioProducto().multiply(BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        venta.setDetalleVenta(detalles);
        venta.setTotalVenta(totalVenta);
        Venta ventaGuardada = ventaRepository.save(venta);
        return mapToDTO(ventaGuardada);
    }

    public List<VentaResponseDTO> buscarVentas(Long idSucursal, LocalDate fecha, boolean soloActivas) {
        if (idSucursal != null && !sucursalRepository.existsById(idSucursal)) {
            throw new SucursalNotFoundException(idSucursal);
        }
        return ventaRepository.findByFiltrosAvanzados(idSucursal,fecha, soloActivas).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public void borrarVentaLogica(Long id) {
        Venta venta = ventaRepository.findById(id).orElseThrow(()-> new VentaNotFoundException(id));

        if (!venta.getActivo()) {
            throw new IllegalStateException("Esta venta ya fue anulada anteriormente");
        }
        for (VentaDetalle ventaDetalle : venta.getDetalleVenta()) {
            Inventario inventario = inventarioRepository.findBySucursalAndProducto(venta.getSucursal(),ventaDetalle.getProducto())
                    .orElseThrow(()-> new IllegalStateException("Error de integridad: no se encuentra inventario para reponer stock"));
            inventario.setCantidad(inventario.getCantidad() +ventaDetalle.getCantidad());
            inventarioRepository.save(inventario);
        }
        venta.setActivo(false);
        ventaRepository.save(venta);
    }

    private VentaResponseDTO mapToDTO(Venta v) {
        String nombreSucursal = (v.getSucursal() != null) ? v.getSucursal().getNombreSucursal() : "Sin sucursal";
        List<DetalleVentaResponseDTO> detallesDTO = v.getDetalleVenta().stream()
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
                v.getActivo(),
                detallesDTO
        );
    }
}
