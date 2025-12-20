package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.AuthService;
import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.infrastructure.in.web.dto.GoogleLoginRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.LoginRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.LoginResponse;
import com.fixa.fixa_api.infrastructure.in.web.dto.RegisterRequest;
import com.fixa.fixa_api.infrastructure.security.GoogleTokenVerifierService;
import com.fixa.fixa_api.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    public AuthController(AuthService authService,
            JwtTokenProvider jwtTokenProvider,
            GoogleTokenVerifierService googleTokenVerifierService) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@Valid @RequestBody RegisterRequest req) {
        Usuario u = authService.register(
                req.getNombre(),
                req.getApellido(),
                req.getEmail(),
                req.getTelefono(),
                req.getPassword());
        return ResponseEntity.ok(u);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Usuario u = authService.login(req.getEmail(), req.getPassword());
        String accessToken = jwtTokenProvider.generateToken(u);
        var refreshToken = authService.createRefreshToken(u);
        return ResponseEntity
                .ok(new LoginResponse(u.getId(), u.getEmail(), u.getRol(), accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody com.fixa.fixa_api.infrastructure.in.web.dto.RefreshTokenRequest req) {
        LoginResponse response = authService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest req) {
        var googleInfo = googleTokenVerifierService.verify(req.getIdToken());

        String nombre = googleInfo.getGivenName();
        String apellido = googleInfo.getFamilyName();
        if ((nombre == null || nombre.isBlank()) && googleInfo.getFullName() != null) {
            nombre = googleInfo.getFullName();
        }

        Usuario u = authService.loginOrRegisterGoogle(
                googleInfo.getEmail(),
                nombre,
                apellido);

        String accessToken = jwtTokenProvider.generateToken(u);
        var refreshToken = authService.createRefreshToken(u);
        return ResponseEntity
                .ok(new LoginResponse(u.getId(), u.getEmail(), u.getRol(), accessToken, refreshToken.getToken()));
    }
}
