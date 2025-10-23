package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioService {

    private final ServicioRepositoryPort servicioPort;

    public ServicioService(ServicioRepositoryPort servicioPort) {
        this.servicioPort = servicioPort;
    }

    public List<Servicio> listarPorEmpresa(Long empresaId) {
        return servicioPort.findByEmpresaId(empresaId);
    }

    public List<Servicio> listarPorEmpresa(Long empresaId, Boolean activo) {
        List<Servicio> base = servicioPort.findByEmpresaId(empresaId);
        if (activo == null) return base;
        return base.stream().filter(s -> s.isActivo() == activo).collect(Collectors.toList());
    }

    public List<Servicio> listarPorEmpresaPaginado(Long empresaId, Boolean activo, Integer page, Integer size) {
        List<Servicio> filtrado = listarPorEmpresa(empresaId, activo);
        if (page == null || size == null || page < 0 || size <= 0) return filtrado;
        int from = Math.min(page * size, filtrado.size());
        int to = Math.min(from + size, filtrado.size());
        return filtrado.subList(from, to);
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
