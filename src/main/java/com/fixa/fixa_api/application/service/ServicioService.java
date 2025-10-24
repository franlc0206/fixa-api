package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioService {

    private final ServicioRepositoryPort servicioPort;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;
    private final CurrentUserService currentUserService;

    public ServicioService(ServicioRepositoryPort servicioPort,
                           UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
                           CurrentUserService currentUserService) {
        this.servicioPort = servicioPort;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.currentUserService = currentUserService;
    }

    public List<Servicio> listarPorEmpresa(Long empresaId) {
        validarPertenencia(empresaId);
        return servicioPort.findByEmpresaId(empresaId);
    }

    public List<Servicio> listarPorEmpresa(Long empresaId, Boolean activo) {
        validarPertenencia(empresaId);
        List<Servicio> base = servicioPort.findByEmpresaId(empresaId);
        if (activo == null) return base;
        return base.stream().filter(s -> s.isActivo() == activo).collect(Collectors.toList());
    }

    public List<Servicio> listarPorEmpresaPaginado(Long empresaId, Boolean activo, Integer page, Integer size) {
        validarPertenencia(empresaId);
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
        if (servicio.getEmpresaId() != null) {
            validarPertenencia(servicio.getEmpresaId());
        }
        return servicioPort.save(servicio);
    }

    public boolean eliminar(Long id) {
        Optional<Servicio> s = servicioPort.findById(id);
        if (s.isEmpty()) return false;
        if (s.get().getEmpresaId() != null) {
            validarPertenencia(s.get().getEmpresaId());
        }
        servicioPort.deleteById(id);
        return true;
    }

    private void validarPertenencia(Long empresaId) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        boolean pertenece = usuarioEmpresaPort.existsByUsuarioAndEmpresa(userId, empresaId);
        if (!pertenece) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No pertenece a la empresa");
        }
    }
}
