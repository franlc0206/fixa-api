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
    public void procesarWebhook(Map<String, Object> payload, Map<String, String> queryParams, String signature,
            String requestId) {
        // ID de la notificación (para log e idempotencia)
        String idNotif = payload.get("id") != null ? String.valueOf(payload.get("id")) : null;

        // ID del recurso (para buscar en la API: el pago o la suscripción)
        String resourceId = null;
        if (payload.get("data") != null && payload.get("data") instanceof Map) {
            Map<?, ?> data = (Map<?, ?>) payload.get("data");
            resourceId = data.get("id") != null ? String.valueOf(data.get("id")) : null;
        }

        String type = (String) payload.get("type");

        if (idNotif == null || type == null) {
            log.warn("Notificación inválida recibida: idNotif o type ausentes. Payload: {}", payload);
            return;
        }

        // 1. Idempotencia: Evitar procesar lo mismo dos veces
        if (notificationLogRepository.existsById(idNotif)) {
            log.info("Notificación {} ya procesada. Saltando.", idNotif);
            return;
        }

        // Validar firma si el secret está configurado
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            // El ID para el manifest de la firma DEBE venir del query param (data.id) o del
            // body (data.id) segun documentacion V2
            String idParaFirma = queryParams != null ? queryParams.get("data.id") : resourceId;
            if (idParaFirma == null)
                idParaFirma = idNotif;

            if (!validarFirma(idParaFirma, signature, requestId)) {
                log.warn("Firma de webhook inválida para notificación {}. Ignorando.", idNotif);
                return;
            }
        }

        // 2. Procesar según tipo usando el resourceId
        try {
            if ("subscription_preapproval".equals(type) || "preapproval".equals(type)) {
                if (resourceId != null) {
                    procesarPreapproval(resourceId);
                } else {
                    log.warn("Tipo preapproval recibido sin resourceId en payload.");
                }
            } else if ("subscription_authorized_payment".equals(type)) {
                log.info(
                        "Gatillo de pago de suscripción recibido (authorized_payment): {}. Procesando suscripcion asociada...",
                        resourceId);
                procesarAuthorizedPayment(resourceId);
            } else if ("payment".equals(type)) {
                log.info("Pago recibido: {}. Solo logueamos por ahora.", resourceId);
            } else {
                log.info("Evento de tipo {} recibido y guardado, sin accion asociada.", type);
            }

            // SOLO GUARDAMOS SI LLEGAMOS ACA (si no hubo excepcion)
            // Esto permite que MP reintente si hubo un error 400/500 antes.
            MpNotificationLogEntity logEntry = new MpNotificationLogEntity();
            logEntry.setNotificationId(idNotif);
            logEntry.setProcessedAt(LocalDateTime.now());
            logEntry.setPayload(payload.toString());
            notificationLogRepository.save(logEntry);

        } catch (Exception e) {
            log.error("Error procesando webhook {} de tipo {}: {}", idNotif, type, e.getMessage(), e);
            // No guardamos en el log, para que MP reintente la notificacion
        }
    }

    private void procesarAuthorizedPayment(String paymentId) {
        // Los pagos de suscripciones se consultan en /authorized_payments
        Optional<Map<String, Object>> paymentDataOpt = mercadoPagoPort.getAuthorizedPayment(paymentId);
        if (paymentDataOpt.isEmpty()) {
            log.warn("No se pudo recuperar información del authorized_payment {}", paymentId);
            return;
        }

        Map<String, Object> paymentData = paymentDataOpt.get();
        log.info("Analizando authorized_payment {} para recuperar info de usuario. Payload: {}", paymentId,
                paymentData);

        String preapprovalId = paymentData.get("preapproval_id") != null
                ? String.valueOf(paymentData.get("preapproval_id"))
                : null;
        String externalRef = paymentData.get("external_reference") != null
                ? String.valueOf(paymentData.get("external_reference"))
                : null;

        // Intentar recuperar email del pagador desde el pago
        String email = null;
        if (paymentData.get("payer") != null && paymentData.get("payer") instanceof Map) {
            Map<?, ?> payer = (Map<?, ?>) paymentData.get("payer");
            if (payer.get("email") != null) {
                email = String.valueOf(payer.get("email"));
            }
        }

        Long usuarioId = null;
        Long planId = null;

        if (externalRef != null && externalRef.contains(":")) {
            try {
                String[] parts = externalRef.split(":");
                usuarioId = Long.parseLong(parts[0]);
                planId = Long.parseLong(parts[1]);
                log.info("Usuario {} y Plan {} recuperados exitosamente desde el PAGO {}", usuarioId, planId,
                        paymentId);
            } catch (Exception e) {
                log.warn("Error parseando external_reference del pago: {}. Error: {}", externalRef, e.getMessage());
            }
        } else if (email != null && !email.isBlank()) {
            log.info("No hay external_reference en el pago {}. Buscando usuario por email del pagador: {}", paymentId,
                    email);
            Optional<Usuario> userOpt = usuarioRepository.findByEmail(email.toLowerCase().trim());
            if (userOpt.isPresent()) {
                usuarioId = userOpt.get().getId();
                planId = 2L; // Default Starter
                log.info("Usuario {} recuperado por email desde el PAGO {}", usuarioId, paymentId);
            }
        }

        // Si logramos identificar al usuario por el pago, procedemos directamente
        if (usuarioId != null && preapprovalId != null) {
            log.info("Identificación exitosa vía pago {}. Finalizando alta para suscripción {}.", paymentId,
                    preapprovalId);
            finalizarAltaSuscripcion(usuarioId, planId, preapprovalId);
            return;
        }

        // Si no, caemos en el flujo estándar de procesar la suscripción
        if (preapprovalId != null) {
            log.info("No se halló referencia en pago {}. Consultando suscripción {}...", paymentId, preapprovalId);
            procesarPreapproval(preapprovalId);
        } else {
            log.warn("El pago {} no contiene preapproval_id ni información de usuario suficiente.", paymentId);
        }
    }

    private void procesarPreapproval(String preapprovalId) {
        Optional<Map<String, Object>> mpDataOpt = mercadoPagoPort.getPreapproval(preapprovalId);
        if (mpDataOpt.isEmpty())
            return;

        Map<String, Object> mpData = mpDataOpt.get();
        String status = (String) mpData.get("status");
        log.info("Datos de suscripción {} recuperados. Status: {}. ExternalRef: {}",
                preapprovalId, status, mpData.get("external_reference"));

        // Solo procesamos si está autorizado (pagado/activo)
        if (!"authorized".equals(status)) {
            log.info("Suscripción {} en estado {}. No se requiere acción de alta.", preapprovalId, status);
            return;
        }

        String externalRef = (String) mpData.get("external_reference");
        Long usuarioId = null;
        Long planId = null;

        if (externalRef != null && externalRef.contains(":")) {
            String[] parts = externalRef.split(":");
            usuarioId = Long.parseLong(parts[0]);
            planId = Long.parseLong(parts[1]);
        } else {
            // FALLBACK 1: Intentar por metadata (más robusto que el email y la referencia
            // externa en V2)
            if (mpData.get("metadata") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadataArr = (Map<String, Object>) mpData.get("metadata");
                if (metadataArr.get("user_id") != null) {
                    usuarioId = Long.valueOf(String.valueOf(metadataArr.get("user_id")));
                    planId = metadataArr.get("plan_id") != null
                            ? Long.valueOf(String.valueOf(metadataArr.get("plan_id")))
                            : 2L;
                    log.info("Usuario recuperado exitosamente desde metadata: ID {}. Plan: {}", usuarioId, planId);
                }
            }

            // FALLBACK 2: Intentar por email (si metadata falló)
            if (usuarioId == null) {
                log.warn("Suscripción {} sin external_reference ni metadata válida. Intentando por email.",
                        preapprovalId);
                log.info("Payload completo de la suscripción de MP: {}", mpData);

                String email = null;
                // 1. Intentar desde payer_email (root)
                if (mpData.get("payer_email") != null && !String.valueOf(mpData.get("payer_email")).isBlank()) {
                    email = String.valueOf(mpData.get("payer_email"));
                }

                // 2. Intentar desde payer object (nested)
                if (email == null && mpData.get("payer") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payer = (Map<String, Object>) mpData.get("payer");
                    if (payer.get("email") != null) {
                        email = String.valueOf(payer.get("email"));
                    }
                }

                if (email != null && !email.isBlank()) {
                    email = email.toLowerCase().trim();
                    log.info("Buscando usuario por email recuperado: '{}'", email);
                    Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
                    if (userOpt.isPresent()) {
                        usuarioId = userOpt.get().getId();
                        planId = 2L; // Default Starter
                        log.info("Usuario recuperado por email ({}): ID {}. Usando Plan ID {} predeterminado.", email,
                                usuarioId, planId);
                    } else {
                        log.warn("No se encontró ningún usuario en la DB con el email: '{}'", email);
                    }
                } else {
                    log.error("No se encontró email del pagador en ningún campo (payer_email ni payer.email).");
                }
            }
        }

        if (usuarioId == null) {
            log.error("No se pudo identificar al usuario para la suscripción {}.", preapprovalId);
            return;
        }

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

            // Normalizar ID: si es alfanumérico debe ir en minúsculas en el manifest
            String normalizedId = id.toLowerCase();

            // Construir el manifest: id:[id];request-id:[x-request-id];ts:[ts];
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", normalizedId, requestId, ts);
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
