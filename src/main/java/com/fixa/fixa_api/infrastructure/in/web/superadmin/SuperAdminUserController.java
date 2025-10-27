package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.application.service.SuperAdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/users")
public class SuperAdminUserController {

    private final SuperAdminUserService userService;

    public SuperAdminUserController(SuperAdminUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> list() {
        return ResponseEntity.ok(userService.list());
    }

    public static class CreateUserRequest {
        @NotBlank public String nombre;
        @NotBlank public String apellido;
        @Email @NotBlank public String email;
        public String telefono;
        @NotBlank public String rol; // SUPERADMIN | EMPRESA | EMPLEADO | CLIENTE
        public boolean activo = true;
    }

    @PostMapping
    public ResponseEntity<Usuario> create(@Valid @RequestBody CreateUserRequest req) {
        Usuario saved = userService.create(req.nombre, req.apellido, req.email, req.telefono, req.rol, req.activo);
        return ResponseEntity.ok(saved);
    }

    public static class UpdateUserRequest {
        public String nombre;
        public String apellido;
        public String telefono;
        public String rol;
        public Boolean activo;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        return userService.update(id, req.nombre, req.apellido, req.telefono, req.rol, req.activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        boolean ok = userService.activar(id, activo);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
