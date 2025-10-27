package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.application.service.SuperAdminCategoriaService;
import com.fixa.fixa_api.domain.model.Categoria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/categorias")
public class SuperAdminCategoriaController {

    private final SuperAdminCategoriaService service;

    public SuperAdminCategoriaController(SuperAdminCategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> list() {
        return ResponseEntity.ok(service.list());
    }

    public static class CreateCategoriaRequest {
        @NotBlank public String tipo;      // empresa | servicio
        @NotBlank public String nombre;
        public String descripcion;
        public boolean activo = true;
    }

    @PostMapping
    public ResponseEntity<Categoria> create(@Valid @RequestBody CreateCategoriaRequest req) {
        Categoria saved = service.create(req.tipo, req.nombre, req.descripcion, req.activo);
        return ResponseEntity.ok(saved);
    }

    public static class UpdateCategoriaRequest {
        public String tipo;
        public String nombre;
        public String descripcion;
        public Boolean activo;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> update(@PathVariable Long id, @RequestBody UpdateCategoriaRequest req) {
        return service.update(id, req.tipo, req.nombre, req.descripcion, req.activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        boolean ok = service.activar(id, activo);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
