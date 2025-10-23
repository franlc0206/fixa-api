package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.AuthService;
import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.infrastructure.in.web.dto.LoginRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.LoginResponse;
import com.fixa.fixa_api.infrastructure.in.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@Valid @RequestBody RegisterRequest req) {
        Usuario u = authService.register(
                req.getNombre(),
                req.getApellido(),
                req.getEmail(),
                req.getTelefono(),
                req.getPassword(),
                req.getRol()
        );
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Usuario u = authService.login(req.getEmail(), req.getPassword());
        // Para Fase 1 devolvemos datos básicos; en Fase 2 se puede emitir JWT
        return ResponseEntity.ok(new LoginResponse(u.getId(), u.getEmail(), u.getRol()));
    }
}
