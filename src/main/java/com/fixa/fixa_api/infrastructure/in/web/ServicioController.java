package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.infrastructure.in.web.dto.ServicioRequest;
import com.fixa.fixa_api.application.service.ServicioService;
import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ServicioController {

    private final ServicioService servicioService;
    private final CurrentUserService currentUserService;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;

    public ServicioController(ServicioService servicioService,
            CurrentUserService currentUserService,
            UsuarioEmpresaRepositoryPort usuarioEmpresaPort) {
        this.servicioService = servicioService;
        this.currentUserService = currentUserService;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
    }

    private void validarAccesoEmpresa(Long empresaId) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        if (!usuarioEmpresaPort.existsByUsuarioAndEmpresa(userId, empresaId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a esta empresa");
        }
    }

    @GetMapping("/api/empresas/{empresaId}/servicios")
    public ResponseEntity<List<Servicio>> listar(@PathVariable Long empresaId,
            @RequestParam(value = "activo", required = false) Boolean activo,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        validarAccesoEmpresa(empresaId);
        if (page != null && size != null) {
            return ResponseEntity.ok(servicioService.listarPorEmpresaPaginado(empresaId, activo, page, size));
        }
        if (activo == null) {
            return ResponseEntity.ok(servicioService.listarPorEmpresa(empresaId));
        }
        return ResponseEntity.ok(servicioService.listarPorEmpresa(empresaId, activo));
    }

    @PostMapping("/api/empresas/{empresaId}/servicios")
    public ResponseEntity<Servicio> crear(@PathVariable Long empresaId, @Valid @RequestBody ServicioRequest req) {
        validarAccesoEmpresa(empresaId);
        Servicio d = new Servicio();
        d.setEmpresaId(empresaId);
        d.setNombre(req.getNombre());
        d.setDescripcion(req.getDescripcion());
        d.setDuracionMinutos(req.getDuracionMinutos());
        d.setRequiereEspacioLibre(req.isRequiereEspacioLibre());
        d.setCosto(req.getCosto());
        d.setRequiereSena(req.isRequiereSena());
        d.setActivo(req.isActivo());
        d.setCategoriaId(req.getCategoriaId());
        d.setFotoUrl(req.getFotoUrl());
        d.setPatronBloques(req.getPatronBloques());
        return ResponseEntity.ok(servicioService.guardar(d));
    }

    @GetMapping("/api/servicios/{id}")
    public ResponseEntity<Servicio> obtener(@PathVariable Long id) {
        // TODO: Validar que el servicio pertenezca a una empresa del usuario
        return servicioService.obtener(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/servicios/{id}")
    public ResponseEntity<Servicio> actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest req) {
        // TODO: Validar que el servicio pertenezca a una empresa del usuario
        return servicioService.obtener(id).map(existing -> {
            Servicio d = new Servicio();
            d.setId(id);
            d.setEmpresaId(req.getEmpresaId());
            d.setNombre(req.getNombre());
            d.setDescripcion(req.getDescripcion());
            d.setDuracionMinutos(req.getDuracionMinutos());
            d.setRequiereEspacioLibre(req.isRequiereEspacioLibre());
            d.setCosto(req.getCosto());
            d.setRequiereSena(req.isRequiereSena());
            d.setActivo(req.isActivo());
            d.setCategoriaId(req.getCategoriaId());
            d.setFotoUrl(req.getFotoUrl());
            d.setPatronBloques(req.getPatronBloques());
            return ResponseEntity.ok(servicioService.guardar(d));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/servicios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        // TODO: Validar que el servicio pertenezca a una empresa del usuario
        if (!servicioService.eliminar(id))
            return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    // Nuevos endpoints anidados por empresa (consistencia REST + guards FE)
    @GetMapping("/api/empresas/{empresaId}/servicios/{id}")
    public ResponseEntity<Servicio> obtenerPorEmpresa(@PathVariable Long empresaId, @PathVariable Long id) {
        validarAccesoEmpresa(empresaId);
        return servicioService.obtener(id)
                .filter(s -> s.getEmpresaId() != null && s.getEmpresaId().equals(empresaId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/empresas/{empresaId}/servicios/{id}")
    public ResponseEntity<Servicio> actualizarPorEmpresa(@PathVariable Long empresaId,
            @PathVariable Long id,
            @Valid @RequestBody ServicioRequest req) {
        validarAccesoEmpresa(empresaId);
        return servicioService.obtener(id)
                .filter(s -> s.getEmpresaId() != null && s.getEmpresaId().equals(empresaId))
                .map(existing -> {
                    Servicio d = new Servicio();
                    d.setId(id);
                    d.setEmpresaId(empresaId); // se toma del path para evitar tampering
                    d.setNombre(req.getNombre());
                    d.setDescripcion(req.getDescripcion());
                    d.setDuracionMinutos(req.getDuracionMinutos());
                    d.setRequiereEspacioLibre(req.isRequiereEspacioLibre());
                    d.setCosto(req.getCosto());
                    d.setRequiereSena(req.isRequiereSena());
                    d.setActivo(req.isActivo());
                    d.setCategoriaId(req.getCategoriaId());
                    d.setFotoUrl(req.getFotoUrl());
                    d.setPatronBloques(req.getPatronBloques());
                    return ResponseEntity.ok(servicioService.guardar(d));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
