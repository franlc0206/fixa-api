package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    private final EmpleadoRepositoryPort empleadoPort;

    public EmpleadoService(EmpleadoRepositoryPort empleadoPort) {
        this.empleadoPort = empleadoPort;
    }

    public List<Empleado> listarPorEmpresa(Long empresaId) {
        return empleadoPort.findByEmpresaId(empresaId);
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
