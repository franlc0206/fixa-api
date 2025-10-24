package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {

    private final EmpleadoRepositoryPort empleadoPort;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;
    private final CurrentUserService currentUserService;

    public EmpleadoService(EmpleadoRepositoryPort empleadoPort,
                           UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
                           CurrentUserService currentUserService) {
        this.empleadoPort = empleadoPort;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.currentUserService = currentUserService;
    }

    public List<Empleado> listarPorEmpresa(Long empresaId) {
        validarPertenencia(empresaId);
        return empleadoPort.findByEmpresaId(empresaId);
    }

    public List<Empleado> listarPorEmpresa(Long empresaId, Boolean activo) {
        validarPertenencia(empresaId);
        List<Empleado> base = empleadoPort.findByEmpresaId(empresaId);
        if (activo == null) return base;
        return base.stream().filter(e -> e.isActivo() == activo).collect(Collectors.toList());
    }

    public List<Empleado> listarPorEmpresaPaginado(Long empresaId, Boolean activo, Integer page, Integer size) {
        validarPertenencia(empresaId);
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
        if (empleado.getEmpresaId() != null) {
            validarPertenencia(empleado.getEmpresaId());
        }
        return empleadoPort.save(empleado);
    }

    public boolean eliminar(Long id) {
        Optional<Empleado> e = empleadoPort.findById(id);
        if (e.isEmpty()) return false;
        if (e.get().getEmpresaId() != null) {
            validarPertenencia(e.get().getEmpresaId());
        }
        empleadoPort.deleteById(id);
        return true;
    }

    private void validarPertenencia(Long empresaId) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        // SUPERADMIN queda exento (si en el futuro agregamos verificación de rol global, se puede inyectar desde SecurityContext)
        boolean pertenece = usuarioEmpresaPort.existsByUsuarioAndEmpresa(userId, empresaId);
        if (!pertenece) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No pertenece a la empresa");
        }
    }
}
