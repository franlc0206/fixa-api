package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {

    private final EmpleadoRepositoryPort empleadoPort;

    public EmpleadoService(EmpleadoRepositoryPort empleadoPort) {
        this.empleadoPort = empleadoPort;
    }

    public List<Empleado> listarPorEmpresa(Long empresaId) {
        return empleadoPort.findByEmpresaId(empresaId);
    }

    public List<Empleado> listarPorEmpresa(Long empresaId, Boolean activo) {
        List<Empleado> base = empleadoPort.findByEmpresaId(empresaId);
        if (activo == null) return base;
        return base.stream().filter(e -> e.isActivo() == activo).collect(Collectors.toList());
    }

    public List<Empleado> listarPorEmpresaPaginado(Long empresaId, Boolean activo, Integer page, Integer size) {
        List<Empleado> filtrado = listarPorEmpresa(empresaId, activo);
        if (page == null || size == null || page < 0 || size <= 0) return filtrado;
        int from = Math.min(page * size, filtrado.size());
        int to = Math.min(from + size, filtrado.size());
        return filtrado.subList(from, to);
    }

    public Optional<Empleado> obtener(Long id) {
        return empleadoPort.findById(id);
    }

    public Empleado guardar(Empleado empleado) {
        return empleadoPort.save(empleado);
    }

    public boolean eliminar(Long id) {
        if (!empleadoPort.existsById(id)) return false;
        empleadoPort.deleteById(id);
        return true;
    }
}
