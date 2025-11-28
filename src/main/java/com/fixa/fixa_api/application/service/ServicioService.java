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
    private final SuscripcionService suscripcionService;

    public ServicioService(ServicioRepositoryPort servicioPort,
            UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
            CurrentUserService currentUserService,
            SuscripcionService suscripcionService) {
        this.servicioPort = servicioPort;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.currentUserService = currentUserService;
        this.suscripcionService = suscripcionService;
    }

    public List<Servicio> listarPorEmpresa(Long empresaId) {
        validarPertenencia(empresaId);
        return servicioPort.findByEmpresaId(empresaId);
    }

    public List<Servicio> listarPorEmpresa(Long empresaId, Boolean activo) {
        validarPertenencia(empresaId);
        List<Servicio> base = servicioPort.findByEmpresaId(empresaId);
        if (activo == null)
            return base;
        return base.stream().filter(s -> s.isActivo() == activo).collect(Collectors.toList());
    }

    public List<Servicio> listarPorEmpresaPaginado(Long empresaId, Boolean activo, Integer page, Integer size) {
        validarPertenencia(empresaId);
        List<Servicio> filtrado = listarPorEmpresa(empresaId, activo);
        if (page == null || size == null || page < 0 || size <= 0)
            return filtrado;
        int start = page * size;
        int end = Math.min(start + size, filtrado.size());
        if (start > filtrado.size())
            return List.of();
        return filtrado.subList(start, end);
    }

    // ============ MÉTODOS PÚBLICOS (sin validación de pertenencia) ============

    /**
     * Listar servicios públicos por empresa (sin autenticación requerida)
     * Para uso en endpoints públicos como /api/public/empresas/{id}/servicios
     */
    public List<Servicio> listarPorEmpresaPublico(Long empresaId, Boolean activo) {
        List<Servicio> base = servicioPort.findByEmpresaId(empresaId);
        if (activo == null)
            return base;
        return base.stream().filter(s -> s.isActivo() == activo).collect(Collectors.toList());
    }

    /**
     * Listar servicios públicos con paginación (sin autenticación requerida)
     */
    public List<Servicio> listarPorEmpresaPaginadoPublico(Long empresaId, Boolean activo, Integer page, Integer size) {
        List<Servicio> filtrado = listarPorEmpresaPublico(empresaId, activo);
        if (page == null || size == null || page < 0 || size <= 0)
            return filtrado;
        int start = page * size;
        int end = Math.min(start + size, filtrado.size());
        if (start > filtrado.size())
            return List.of();
        return filtrado.subList(start, end);
    }

    public Optional<Servicio> obtener(Long id) {
        return servicioPort.findById(id);
    }

    public Servicio guardar(Servicio servicio) {
        if (servicio.getEmpresaId() != null) {
            validarPertenencia(servicio.getEmpresaId());
        }

        // Validación de límites del plan (solo al crear nuevo servicio)
        if (servicio.getId() == null && servicio.getEmpresaId() != null) {
            var plan = suscripcionService.obtenerPlanActual(servicio.getEmpresaId());
            long serviciosActuales = servicioPort.findByEmpresaId(servicio.getEmpresaId()).stream()
                    .filter(Servicio::isActivo)
                    .count();

            if (serviciosActuales >= plan.getMaxServicios()) {
                throw new ApiException(HttpStatus.FORBIDDEN, "PLAN_LIMIT_REACHED",
                        String.format(
                                "Has alcanzado el límite de servicios de tu plan (%d/%d). Actualiza tu plan para agregar más.",
                                serviciosActuales, plan.getMaxServicios()));
            }
        }

        return servicioPort.save(servicio);
    }

    public boolean eliminar(Long id) {
        Optional<Servicio> s = servicioPort.findById(id);
        if (s.isEmpty())
            return false;
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
