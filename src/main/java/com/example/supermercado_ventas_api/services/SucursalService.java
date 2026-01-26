package com.example.supermercado_ventas_api.services;

import com.example.supermercado_ventas_api.exceptions.SucursalNotFoundException;
import com.example.supermercado_ventas_api.models.Sucursal;
import com.example.supermercado_ventas_api.repositories.SucursalRepository;
import com.example.supermercado_ventas_api.repositories.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalService {
    private final SucursalRepository sucursalRepository;
    private final VentaRepository ventaRepository;

    public List<Sucursal> findAll() {
        return sucursalRepository.findAll();
    }

    public Sucursal findById(Long id) {
        return sucursalRepository.findById(id).orElseThrow(() -> new SucursalNotFoundException(id));
    }

    @Transactional
    public Sucursal create(Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    @Transactional
    public Sucursal update(Long id, Sucursal sucursal) {
        Sucursal sucursalExistente = findById(id);
        sucursalExistente.setNombreSucursal(sucursal.getNombreSucursal());
        sucursalExistente.setDireccion(sucursal.getDireccion());
        return sucursalRepository.save(sucursalExistente);
    }

    @Transactional
    public void delete(Long id) {
        if (!sucursalRepository.existsById(id)) {
            throw new SucursalNotFoundException(id);
        }

        if (ventaRepository.existsBySucursal_Id(id)) {
            throw new IllegalStateException("No se puede eliminar la sucursal porque tiene ventas asociadas.");
        }
        sucursalRepository.deleteById(id);
    }
}
