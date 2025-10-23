package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    private final ServicioRepositoryPort servicioPort;

    public ServicioService(ServicioRepositoryPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    public List<Servicio> listarPorEmpresa(Long empresaId) {
        return servicioPort.findByEmpresaId(empresaId);
    }

    public Optional<Servicio> obtener(Long id) {
        return servicioPort.findById(id);
    }

    public Servicio guardar(Servicio servicio) {
        return servicioPort.save(servicio);
    }

    public boolean eliminar(Long id) {
        if (!servicioPort.existsById(id)) return false;
        servicioPort.deleteById(id);
        return true;
    }
}
