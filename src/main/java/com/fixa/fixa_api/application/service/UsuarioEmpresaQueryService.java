package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioEmpresaQueryService {

    private final CurrentUserService currentUserService;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;
    private final EmpresaRepositoryPort empresaPort;

    public UsuarioEmpresaQueryService(CurrentUserService currentUserService,
                                      UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
                                      EmpresaRepositoryPort empresaPort) {
        this.currentUserService = currentUserService;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.empresaPort = empresaPort;
    }

    public List<Empresa> empresasDelUsuarioActual() {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        List<Long> empresaIds = usuarioEmpresaPort.findEmpresaIdsByUsuario(userId);
        return empresaIds.stream()
                .map(empresaPort::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
