package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.OnboardingService;
import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public OnboardingController(OnboardingService onboardingService, UsuarioRepositoryPort usuarioRepositoryPort) {
        this.onboardingService = onboardingService;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Spring Security UserDetails suele ser el email
        return usuarioRepositoryPort.findByEmail(email)
                .map(Usuario::getId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @GetMapping
    public ResponseEntity<OnboardingService.OnboardingStatusDTO> getOnboardingStatus() {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(onboardingService.getProgreso(userId));
    }

    @PostMapping("/{featureKey}/complete")
    public ResponseEntity<Void> completeStep(@PathVariable String featureKey) {
        Long userId = getAuthenticatedUserId();
        onboardingService.completarPaso(userId, featureKey);
        return ResponseEntity.ok().build();
    }

}
