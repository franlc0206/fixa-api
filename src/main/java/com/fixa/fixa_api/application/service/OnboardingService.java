package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.model.UsuarioOnboardingProgreso;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.OnboardingRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.notification.NotificationClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    private final OnboardingRepositoryPort onboardingRepositoryPort;
    private final EmpresaRepositoryPort empresaRepositoryPort;
    private final EmpleadoRepositoryPort empleadoRepositoryPort;
    private final ServicioRepositoryPort servicioRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final NotificationClient notificationClient;

    public static final String KEY_INITIAL_SETUP = "INITIAL_SETUP";
    public static final String KEY_TOUR_EMPLEADOS = "TOUR_EMPLEADOS";
    public static final String KEY_TOUR_SERVICIOS = "TOUR_SERVICIOS";
    public static final String KEY_TOUR_DISPONIBILIDAD = "TOUR_DISPONIBILIDAD";

    public OnboardingService(OnboardingRepositoryPort onboardingRepositoryPort,
            EmpresaRepositoryPort empresaRepositoryPort,
            EmpleadoRepositoryPort empleadoRepositoryPort,
            ServicioRepositoryPort servicioRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort,
            NotificationClient notificationClient) {
        this.onboardingRepositoryPort = onboardingRepositoryPort;
        this.empresaRepositoryPort = empresaRepositoryPort;
        this.empleadoRepositoryPort = empleadoRepositoryPort;
        this.servicioRepositoryPort = servicioRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.notificationClient = notificationClient;
    }

    @Transactional(readOnly = true)
    public OnboardingStatusDTO getProgreso(Long usuarioId) {
        List<UsuarioOnboardingProgreso> progresoList = onboardingRepositoryPort.findByUsuarioId(usuarioId);
        List<String> completedSteps = progresoList.stream()
                .filter(UsuarioOnboardingProgreso::getCompletado)
                .map(UsuarioOnboardingProgreso::getFeatureKey)
                .collect(Collectors.toList());

        String suggestedStep = null;

        // Smart Detection Logic
        Optional<Empresa> empresaOpt = empresaRepositoryPort.findByUsuarioAdminId(usuarioId);
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            int empleados = empleadoRepositoryPort.findByEmpresaId(empresa.getId()).size();
            int servicios = servicioRepositoryPort.findByEmpresaId(empresa.getId()).size();

            if (!completedSteps.contains(KEY_INITIAL_SETUP)) {
                suggestedStep = KEY_INITIAL_SETUP;
            } else if (empleados == 0 && !completedSteps.contains(KEY_TOUR_EMPLEADOS)) {
                suggestedStep = KEY_TOUR_EMPLEADOS;
            } else if (servicios == 0 && !completedSteps.contains(KEY_TOUR_SERVICIOS)) {
                suggestedStep = KEY_TOUR_SERVICIOS;
            } else if (!completedSteps.contains(KEY_TOUR_DISPONIBILIDAD)) {
                suggestedStep = KEY_TOUR_DISPONIBILIDAD;
            }
        } else {
            // Si no tiene empresa, quizas el paso inicial es crearla, pero asumimos
            // INITIAL_SETUP por defecto
            if (!completedSteps.contains(KEY_INITIAL_SETUP)) {
                suggestedStep = KEY_INITIAL_SETUP;
            }
        }

        return new OnboardingStatusDTO(completedSteps, suggestedStep);
    }

    @Transactional
    public void completarPaso(Long usuarioId, String featureKey) {
        Optional<UsuarioOnboardingProgreso> existing = onboardingRepositoryPort.findByUsuarioIdAndFeatureKey(usuarioId,
                featureKey);

        if (existing.isPresent() && existing.get().getCompletado()) {
            return; // Ya completado
        }

        UsuarioOnboardingProgreso progreso = existing.orElse(new UsuarioOnboardingProgreso());
        progreso.setUsuarioId(usuarioId);
        progreso.setFeatureKey(featureKey);
        progreso.setCompletado(true);
        progreso.setFechaCompletado(LocalDateTime.now());

        onboardingRepositoryPort.save(progreso);

        // Trigger Notification on INITIAL_SETUP completion
        if (KEY_INITIAL_SETUP.equals(featureKey)) {
            usuarioRepositoryPort.findById(usuarioId).ifPresent(usuario -> {
                String fullName = usuario.getNombre() + " " + usuario.getApellido();
                notificationClient.sendWelcomeEmail(usuario.getEmail(), fullName.trim());
            });
        }
    }

    // DTO simple interno o compartido
    public static class OnboardingStatusDTO {
        private final List<String> completedSteps;
        private final String suggestedStep;

        public OnboardingStatusDTO(List<String> completedSteps, String suggestedStep) {
            this.completedSteps = completedSteps != null ? completedSteps : Collections.emptyList();
            this.suggestedStep = suggestedStep;
        }

        public List<String> getCompletedSteps() {
            return completedSteps;
        }

        public String getSuggestedStep() {
            return suggestedStep;
        }
    }
}
