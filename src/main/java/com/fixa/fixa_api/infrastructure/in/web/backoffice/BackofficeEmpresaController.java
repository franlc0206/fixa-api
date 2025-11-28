package com.fixa.fixa_api.infrastructure.in.web.backoffice;

import com.fixa.fixa_api.application.service.UsuarioEmpresaQueryService;
import com.fixa.fixa_api.domain.model.Empresa;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/backoffice/empresas")
public class BackofficeEmpresaController {

    private final UsuarioEmpresaQueryService usuarioEmpresaQueryService;

    public BackofficeEmpresaController(UsuarioEmpresaQueryService usuarioEmpresaQueryService) {
        this.usuarioEmpresaQueryService = usuarioEmpresaQueryService;
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> misEmpresas() {
        return ResponseEntity.ok(usuarioEmpresaQueryService.empresasDelUsuarioActual());
    }
}
