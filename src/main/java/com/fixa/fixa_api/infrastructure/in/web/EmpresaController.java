package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.EmpresaService;
import com.fixa.fixa_api.domain.model.Empresa;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listar(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        List<Empresa> result = empresaService.listarPublicasConSuscripcionActivaPaginado(categoriaId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtener(@PathVariable Long id) {
        // Para detalle público, solo devolvemos si la empresa está visible, activa y con suscripción activa.
        return empresaService.obtener(id)
                .filter(e -> Boolean.TRUE.equals(e.isVisibilidadPublica()) && e.isActivo())
                .filter(e -> empresaService.tieneSuscripcionActiva(e.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
