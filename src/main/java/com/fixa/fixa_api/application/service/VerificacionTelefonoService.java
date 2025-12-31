package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.application.usecase.ConfirmarCodigoUseCase;
import com.fixa.fixa_api.application.usecase.CrearVerificacionUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.model.VerificacionTelefono;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.domain.repository.VerificacionTelefonoRepositoryPort;
import com.fixa.fixa_api.domain.service.NotificationServicePort;
import com.fixa.fixa_api.domain.service.SmsServicePort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de aplicación que implementa los use cases de verificación
 * telefónica.
 * Siguiendo arquitectura hexagonal, este servicio:
 * - Solo depende de puertos (interfaces) del dominio
 * - No conoce detalles de implementación de infraestructura
 * - Contiene lógica de negocio relacionada con verificaciones telefónicas
 */
@Service
public class VerificacionTelefonoService implements CrearVerificacionUseCase, ConfirmarCodigoUseCase {

    private static final int CODIGO_LENGTH = 6;
    private static final int EXPIRACION_MINUTOS = 5;
    private static final int MAX_INTENTOS = 3;
    private static final int RATE_LIMIT_MINUTOS = 5;

    private final VerificacionTelefonoRepositoryPort verificacionPort;
    private final SmsServicePort smsService;
    private final NotificationServicePort notificationService;
    private final TurnoRepositoryPort turnoPort;

    // Rate limiting simple en memoria (en producción usar Redis)
    private final Map<String, RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();

    public VerificacionTelefonoService(
            VerificacionTelefonoRepositoryPort verificacionPort,
            SmsServicePort smsService,
            NotificationServicePort notificationService,
            TurnoRepositoryPort turnoPort) {
        this.verificacionPort = verificacionPort;
        this.smsService = smsService;
        this.notificationService = notificationService;
        this.turnoPort = turnoPort;
    }

    @Override
    @Transactional
    public VerificacionTelefono ejecutar(String telefono, String email, String canal, Long turnoId) {
        // Validar canal
        if (canal == null || (!canal.equalsIgnoreCase("sms") &&
                !canal.equalsIgnoreCase("whatsapp") &&
                !canal.equalsIgnoreCase("email"))) {
            canal = "sms"; // default
        }

        // Validar destino según canal
        if (canal.equalsIgnoreCase("email")) {
            if (email == null || email.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El email es requerido para este canal");
            }
        } else {
            if (telefono == null || telefono.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El teléfono es requerido para este canal");
            }
        }

        // Rate limiting: verificar intentos recientes
        String destino = canal.equalsIgnoreCase("email") ? email : telefono;
        validarRateLimit(destino);

        // Generar código aleatorio de 6 dígitos
        String codigo = generarCodigoAleatorio();

        // Crear verificación
        VerificacionTelefono verificacion = new VerificacionTelefono();
        verificacion.setTelefono(telefono);
        verificacion.setEmail(email);
        verificacion.setCodigo(codigo);
        verificacion.setCanal(canal.toLowerCase());
        verificacion.setFechaEnvio(LocalDateTime.now());
        verificacion.setFechaExpiracion(LocalDateTime.now().plusMinutes(EXPIRACION_MINUTOS));
        verificacion.setValidado(false);
        verificacion.setTurnoId(turnoId);

        // Persistir
        VerificacionTelefono guardada = verificacionPort.save(verificacion);

        // Enviar según canal
        boolean enviado = false;
        if (canal.equalsIgnoreCase("email")) {
            enviado = enviarEmailVerificacion(email, codigo);
        } else {
            enviado = smsService.enviarCodigoVerificacion(telefono, codigo, canal);
        }

        if (!enviado) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo enviar el código de verificación. Por favor, intenta nuevamente.");
        }

        // Registrar intento en rate limit
        registrarIntento(destino);

        return guardada;
    }

    @Override
    @Transactional
    public VerificacionTelefono ejecutar(Long verificacionId, String codigo) {
        // Buscar verificación
        VerificacionTelefono verificacion = verificacionPort.findById(verificacionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Verificación no encontrada"));

        // Validar que no esté ya validada
        if (verificacion.isValidado()) {
            throw new ApiException(HttpStatus.CONFLICT, "Esta verificación ya fue utilizada");
        }

        // Validar expiración
        if (LocalDateTime.now().isAfter(verificacion.getFechaExpiracion())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El código ha expirado. Solicita uno nuevo.");
        }

        // Validar código
        if (!verificacion.getCodigo().equals(codigo)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Código inválido");
        }

        // Marcar como validado
        verificacion.setValidado(true);
        VerificacionTelefono actualizada = verificacionPort.save(verificacion);

        // Si hay turno asociado, actualizar su estado
        if (verificacion.getTurnoId() != null) {
            Turno turno = turnoPort.findById(verificacion.getTurnoId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado"));

            turno.setTelefonoValidado(true);

            // Si el turno estaba pendiente por validación, cambiar a CONFIRMADO o PENDIENTE
            // según regla de empresa
            // Por ahora, si requería validación y ya está validado, lo confirmamos
            if ("PENDIENTE".equalsIgnoreCase(turno.getEstado()) && turno.isRequiereValidacion()) {
                turno.setEstado("CONFIRMADO");
            }

            turnoPort.save(turno);
        }

        return actualizada;
    }

    private boolean enviarEmailVerificacion(String email, String codigo) {
        try {
            String template = "Tu código de verificación para Fixe es: <b>" + codigo + "</b>\n\n" +
                    "Este código expirará en " + EXPIRACION_MINUTOS + " minutos.";

            notificationService.sendEmail(email, template, Map.of("codigo", codigo));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Genera un código numérico aleatorio de 6 dígitos.
     */
    private String generarCodigoAleatorio() {
        SecureRandom random = new SecureRandom();
        int codigo = 100000 + random.nextInt(900000); // Rango: 100000-999999
        return String.valueOf(codigo);
    }

    /**
     * Valida rate limiting para evitar abuso.
     * Máximo MAX_INTENTOS cada RATE_LIMIT_MINUTOS minutos.
     */
    private void validarRateLimit(String telefono) {
        RateLimitInfo info = rateLimitCache.get(telefono);

        if (info == null) {
            return; // Primera vez, permitir
        }

        // Limpiar intentos antiguos (más de RATE_LIMIT_MINUTOS)
        LocalDateTime limiteVentana = LocalDateTime.now().minusMinutes(RATE_LIMIT_MINUTOS);
        info.limpiarIntentosAntiguos(limiteVentana);

        // Validar límite
        if (info.getIntentos() >= MAX_INTENTOS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Por favor, espera " + RATE_LIMIT_MINUTOS
                            + " minutos antes de intentar nuevamente.");
        }
    }

    /**
     * Registra un intento de verificación para rate limiting.
     */
    private void registrarIntento(String telefono) {
        rateLimitCache.compute(telefono, (key, info) -> {
            if (info == null) {
                info = new RateLimitInfo();
            }
            info.agregarIntento();
            return info;
        });
    }

    /**
     * Clase interna para almacenar información de rate limiting.
     * En producción, esto debería estar en Redis con TTL.
     */
    private static class RateLimitInfo {
        private final java.util.List<LocalDateTime> intentos = new java.util.ArrayList<>();

        public void agregarIntento() {
            intentos.add(LocalDateTime.now());
        }

        public int getIntentos() {
            return intentos.size();
        }

        public void limpiarIntentosAntiguos(LocalDateTime limite) {
            intentos.removeIf(fecha -> fecha.isBefore(limite));
        }
    }
}
