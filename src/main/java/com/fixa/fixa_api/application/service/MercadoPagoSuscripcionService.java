package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Plan;
import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.repository.PlanRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.out.mercadopago.MercadoPagoPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.MpNotificationLogEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.MpNotificationLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class MercadoPagoSuscripcionService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoSuscripcionService.class);

    private final MercadoPagoPort mercadoPagoPort;
    private final MpNotificationLogJpaRepository notificationLogRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PlanRepositoryPort planRepository;
    private final EmpresaService empresaService;
    private final SuscripcionService suscripcionService;

    @org.springframework.beans.factory.annotation.Value("${mercadopago.webhook-secret:}")
    private String webhookSecret;

    public MercadoPagoSuscripcionService(MercadoPagoPort mercadoPagoPort,
            MpNotificationLogJpaRepository notificationLogRepository,
            UsuarioRepositoryPort usuarioRepository,
            PlanRepositoryPort planRepository,
            EmpresaService empresaService,
            SuscripcionService suscripcionService) {
        this.mercadoPagoPort = mercadoPagoPort;
        this.notificationLogRepository = notificationLogRepository;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.empresaService = empresaService;
        this.suscripcionService = suscripcionService;
    }

    public String iniciarSuscripcion(Long usuarioId, Long planId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan no encontrado"));

        if (plan.getMercadopagoPlanId() == null || plan.getMercadopagoPlanId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "El plan seleccionado no tiene un ID de Mercado Pago configurado.");
        }

        String link = mercadoPagoPort.createPreapprovalLink(usuario.getEmail(), usuarioId, planId,
                plan.getMercadopagoPlanId());
        if (link == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el link de pago con Mercado Pago.");
        }

        return link;
    }

    @Transactional
    public void procesarWebhook(Map<String, Object> payload, String signature, String requestId) {
        String idNotif = null;
        if (payload.get("id") != null) {
            idNotif = String.valueOf(payload.get("id"));
        } else if (payload.get("data") != null && payload.get("data") instanceof Map
                && ((Map<?, ?>) payload.get("data")).get("id") != null) {
            idNotif = String.valueOf(((Map<?, ?>) payload.get("data")).get("id"));
        }

        String type = (String) payload.get("type");

        if (idNotif == null || type == null)
            return;

        // Validar firma si el secret está configurado
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            if (!validarFirma(idNotif, signature, requestId)) {
                log.warn("Firma de webhook inválida para notificación {}. Ignorando.", idNotif);
                return;
            }
        }

        // 1. Idempotencia: Verificar si ya procesamos esta notificación
        if (notificationLogRepository.existsById(idNotif)) {
            log.info("Notificación {} ya procesada anteriormente.", idNotif);
            return;
        }

        // Registrar notificación
        MpNotificationLogEntity logEntry = new MpNotificationLogEntity();
        logEntry.setNotificationId(idNotif);
        logEntry.setProcessedAt(LocalDateTime.now());
        logEntry.setPayload(payload.toString());
        notificationLogRepository.save(logEntry);

        // 2. Procesar según tipo
        if ("subscription_preapproval".equals(type) || "preapproval".equals(type)) {
            procesarPreapproval(idNotif);
        } else if ("payment".equals(type)) {
            // Podríamos procesar pagos individuales si fuera necesario
            log.info("Pago recibido: {}. Solo logueamos por ahora.", idNotif);
        }
    }

    private void procesarPreapproval(String preapprovalId) {
        Optional<Map<String, Object>> mpDataOpt = mercadoPagoPort.getPreapproval(preapprovalId);
        if (mpDataOpt.isEmpty())
            return;

        Map<String, Object> mpData = mpDataOpt.get();
        String status = (String) mpData.get("status");

        // Solo procesamos si está autorizado (pagado/activo)
        if (!"authorized".equals(status)) {
            log.info("Suscripción {} en estado {}. No se requiere acción.", preapprovalId, status);
            return;
        }

        String externalRef = (String) mpData.get("external_reference");
        if (externalRef == null || !externalRef.contains(":")) {
            log.error("Suscripción {} no tiene external_reference válido.", preapprovalId);
            return;
        }

        String[] parts = externalRef.split(":");
        Long usuarioId = Long.parseLong(parts[0]);
        Long planId = Long.parseLong(parts[1]);

        finalizarAltaSuscripcion(usuarioId, planId, preapprovalId);
    }

    private void finalizarAltaSuscripcion(Long usuarioId, Long planId, String externalId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null)
            return;

        Plan plan = planRepository.findById(planId).orElse(null);
        if (plan == null)
            return;

        log.info("Finalizando alta de suscripción para usuario {} y plan {}.", usuarioId, planId);

        // 1. Crear Empresa
        Empresa empresa = new Empresa();
        empresa.setNombre("Nueva Empresa " + usuario.getNombre());
        empresa.setEmail(usuario.getEmail());
        empresa.setTelefono(usuario.getTelefono());
        empresa.setActivo(true);
        empresa.setVisibilidadPublica(true);
        empresa.setPlanActualId(planId);

        Empresa guardada = empresaService.guardar(empresa);

        // 2. Actualizar Usuario y Vínculo (EmpresaService.guardar ya hace parte de
        // esto, pero aseguramos OWNER)
        // Por ahora, asumimos que el usuario que compra es el dueño.
        // Implementar lógica de UsuarioEmpresa si es necesario.

        // 3. Asignar Plan (SuscripcionService)
        suscripcionService.asignarPlan(guardada.getId(), planId, plan.getPrecio());

        log.info("Empresa {} creada y plan {} asignado exitosamente.", guardada.getId(), planId);
    }

    private boolean validarFirma(String id, String signatureHeader, String requestId) {
        if (signatureHeader == null || requestId == null) {
            log.warn("Firma o Request ID ausentes. Signature: {}, RequestId: {}", signatureHeader, requestId);
            return false;
        }

        try {
            // Extraer ts y v1 del header x-signature (formato: ts=xxx,v1=yyy)
            String ts = null;
            String v1 = null;
            String[] parts = signatureHeader.split(",");
            for (String part : parts) {
                String[] pair = part.split("=");
                if (pair.length != 2)
                    continue;
                if ("ts".equals(pair[0].trim()))
                    ts = pair[1].trim();
                else if ("v1".equals(pair[0].trim()))
                    v1 = pair[1].trim();
            }

            if (ts == null || v1 == null) {
                log.warn("No se pudo extraer ts o v1 del header x-signature: {}", signatureHeader);
                return false;
            }

            // Construir el manifest: id:[id];request-id:[x-request-id];ts:[ts];
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", id, requestId, ts);
            log.debug("Manifest generado para validación: {}", manifest);

            // Generar HMAC SHA256
            javax.crypto.Mac sha256_HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(
                    webhookSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hashBytes = sha256_HMAC.doFinal(manifest.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String generatedSignature = sb.toString();

            boolean isValid = generatedSignature.equals(v1);
            if (!isValid) {
                log.warn("Firma inválida. Manifest: {}, v1(recibida): {}, hmac(generada): {}", manifest, v1,
                        generatedSignature);
                // Tip: Si hmac es totalmente distinto, el secret está mal.
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error crítico validando firma de Mercado Pago: {}", e.getMessage());
            return false;
        }
    }
}
