package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.UsuarioEmpresaQueryService;
import com.fixa.fixa_api.domain.model.Empresa;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UsuarioEmpresaQueryService ueQueryService;

    public MeController(UsuarioEmpresaQueryService ueQueryService) {
        this.ueQueryService = ueQueryService;
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<Empresa>> misEmpresas() {
        return ResponseEntity.ok(ueQueryService.empresasDelUsuarioActual());
    }
}
