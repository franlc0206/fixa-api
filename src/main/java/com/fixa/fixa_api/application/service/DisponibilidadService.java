package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Disponibilidad;
import com.fixa.fixa_api.domain.repository.DisponibilidadRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DisponibilidadService {

    private final DisponibilidadRepositoryPort disponibilidadPort;

    public DisponibilidadService(DisponibilidadRepositoryPort disponibilidadPort) {
        this.disponibilidadPort = disponibilidadPort;
    }

    public List<Disponibilidad> listarPorEmpleado(Long empleadoId) {
        return disponibilidadPort.findByEmpleadoId(empleadoId);
    }

    public Optional<Disponibilidad> obtener(Long id) {
        return disponibilidadPort.findById(id);
    }

    public Disponibilidad guardar(Disponibilidad disponibilidad) {
        return disponibilidadPort.save(disponibilidad);
    }

    public boolean eliminar(Long id) {
        if (!disponibilidadPort.existsById(id)) return false;
        disponibilidadPort.deleteById(id);
        return true;
    }
}
