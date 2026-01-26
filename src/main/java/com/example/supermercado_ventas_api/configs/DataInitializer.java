package com.example.supermercado_ventas_api.configs;

import com.example.supermercado_ventas_api.models.Inventario;
import com.example.supermercado_ventas_api.models.Producto;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.repositories.InventarioRepository;
import com.example.supermercado_ventas_api.repositories.ProductoRepository;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // --- 1. CARGA DE DATOS (Solo si está vacío) ---
        if (sucursalRepository.count() == 0) {
            System.out.println("🚀 Inicializando datos de prueba con STOCK...");

            // Crear Sucursales
            Sucursal s1 = Sucursal.builder().nombreSucursal("Sucursal Centro").direccion("Av.Principal 123").build();
            Sucursal s2 = Sucursal.builder().nombreSucursal("Sucursal Norte").direccion("Calle Industrial 55").build();

            List<Sucursal> sucursals = sucursalRepository.saveAll(List.of(s1, s2));
            s1 = sucursals.get(0);
            s2 = sucursals.get(1);

            // Crear Productos
            Producto p1 = Producto.builder().nombreProducto("Arroz Premium").precioProducto(new BigDecimal("2.50")).categoria("Alimentos").build();
            Producto p2 = Producto.builder().nombreProducto("Leche Entera").precioProducto(new BigDecimal("2.00")).categoria("Lácteos").build();
            Producto p3 = Producto.builder().nombreProducto("Jabón Liquido").precioProducto(new BigDecimal("2.50")).categoria("Limpieza").build();
            Producto p4 = Producto.builder().nombreProducto("Galleta Chocolate").precioProducto(new BigDecimal("3.00")).categoria("Snacks").build();
            Producto p5 = Producto.builder().nombreProducto("Gaseosa").precioProducto(new BigDecimal("2.45")).categoria("Bebidas").build();

            List<Producto> productos = productoRepository.saveAll(List.of(p1, p2, p3, p4, p5));
            p1 = productos.get(0);
            p2 = productos.get(1);
            p3 = productos.get(2);
            p4 = productos.get(3);
            p5 = productos.get(4);

            // Crear Inventarios
            Inventario i1 = Inventario.builder().sucursal(s1).producto(p1).cantidad(50).build();
            Inventario i2 = Inventario.builder().sucursal(s1).producto(p2).cantidad(20).build();
            Inventario i3 = Inventario.builder().sucursal(s1).producto(p3).cantidad(100).build();
            Inventario i4 = Inventario.builder().sucursal(s2).producto(p4).cantidad(5).build();
            Inventario i5 = Inventario.builder().sucursal(s2).producto(p5).cantidad(50).build();
            Inventario i6 = Inventario.builder().sucursal(s2).producto(p1).cantidad(50).build();

            inventarioRepository.saveAll(List.of(i1, i2, i3, i4, i5, i6));

            System.out.println("✅ Datos cargados: Sucursales, Productos e Inventarios");
        }

        // --- 2. CREACIÓN DE VISTAS SQL ---
        try {
            System.out.println("👀 Creando Vistas SQL...");

            String sqlVistasVentas = """
                    CREATE OR REPLACE VIEW resumen_ventas AS
                    SELECT
                         v.id AS folio_venta,
                         v.fecha,
                         s.nombre_sucursal AS sucursal,
                         p.nombre_producto AS producto,
                         p.categoria,
                         p.precio_producto AS precio_unitario,
                         (d.cantidad * p.precio_producto) AS subtotal_linea,
                         v.activa
                    FROM ventas v
                    JOIN sucursales s ON v.sucursal_id = s.id
                    JOIN venta_detalles d ON d.venta_id = v.id
                    JOIN productos p ON d.producto_id = p.id
                    WHERE v.activa = true
                    """;
            jdbcTemplate.execute(sqlVistasVentas);

            // Vista Inventario
            String sqlVistaInventario = """
                    CREATE OR REPLACE VIEW resumen_inventario AS
                    SELECT
                        s.nombre_sucursal AS sucursal,
                        p.nombre_producto AS producto,
                        p.categoria,
                        i.cantidad AS stock_disponible
                    FROM inventario i
                    JOIN sucursales s ON i.id_sucursal = s.id
                    JOIN productos p ON i.id_producto = p.id
                    ORDER BY s.nombre_sucursal, p.nombre_producto
                    """;
            jdbcTemplate.execute(sqlVistaInventario);

            System.out.println("✨ Vistas 'resumen_ventas' y 'resumen_inventario' creadas con éxito.");
        } catch (Exception e) {
            System.out.println("⚠️ Error al crear Vistas SQL: " + e.getMessage());
        }
    }
}
