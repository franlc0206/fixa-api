package com.fixa.fixa_api.infrastructure.out.mercadopago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preapproval.PreapprovalClient;

import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preapproval.Preapproval;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAdapter.class);

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${mercadopago.back-url:https://fixe.com.ar/backoffice/suscripcion/ready}")
    private String backUrl;

    @PostConstruct
    public void init() {
        if (accessToken != null && !accessToken.isBlank()) {
            MercadoPagoConfig.setAccessToken(accessToken);
            log.info("Mercado Pago SDK initialized.");
        } else {
            log.warn("Mercado Pago Access Token is missing!");
        }
    }

    @Override
    @SuppressWarnings("null")
    public String createPreapprovalLink(String userEmail, Long userId, Long planId, String mpPlanId) {
        try {
            String externalRef = userId + ":" + planId;
            String computedBackUrl = (backUrl != null && !backUrl.isBlank()) ? backUrl : "https://fixe.com.ar";

            // DOC NOTE: According to documentation, subscriptions with
            // 'preapproval_plan_id' must be 'authorized'.
            // To get an 'init_point' (link) via 'pending' status, we should create a
            // subscription
            // *without* associating the plan ID directly in the creation, but copying the
            // plan's recurrence details.

            // 1. Fetch Plan Details
            Map<String, Object> plan = getPreapprovalPlan(mpPlanId)
                    .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + mpPlanId));

            @SuppressWarnings("unchecked")
            Map<String, Object> autoRecurring = (Map<String, Object>) plan.get("auto_recurring");
            String reason = (String) plan.get("reason");

            // 2. Create Standalone Subscription (mimicking the plan)
            String url = "https://api.mercadopago.com/preapproval";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            // WE DO NOT SEND "preapproval_plan_id" so we can use status="pending" and get a
            // link.
            body.put("auto_recurring", autoRecurring);
            body.put("reason", reason);
            body.put("external_reference", externalRef);
            body.put("back_url", computedBackUrl);
            body.put("status", "pending");

            // payer_email removed to allow payment from any MP account; reconciliation uses
            // external_reference.

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ParameterizedTypeReference<Map<String, Object>> mapTypeRef = new ParameterizedTypeReference<>() {
            };
            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    mapTypeRef);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> respBody = response.getBody();
                if (respBody != null) {
                    String initPoint = (String) respBody.get("init_point");
                    if (initPoint != null && !initPoint.isBlank()) {
                        log.info("Preapproval created manually (from plan copy). Redirect to: {}", initPoint);
                        return initPoint;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Fallo creación Preapproval (Manual HTTP): {}", e.getMessage(), e);
            throw new RuntimeException("Error creando suscripción en Mercado Pago: " + e.getMessage(), e);
        }

        throw new RuntimeException("No se pudo obtener el link de suscripción de la respuesta de Mercado Pago.");
    }

    @Override
    public Optional<Map<String, Object>> getPreapproval(String preapprovalId) {
        try {
            PreapprovalClient client = new PreapprovalClient();
            Preapproval preapproval = client.get(preapprovalId);
            return Optional.ofNullable(convertPreapprovalToMap(preapproval));
        } catch (Exception e) {
            log.error("Error fetching Preapproval SDK ({}): {}", preapprovalId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, Object>> getPayment(String paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));
            return Optional.ofNullable(convertPaymentToMap(payment));
        } catch (Exception e) {
            log.error("Error fetching Payment SDK ({}): {}", paymentId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, Object>> getAuthorizedPayment(String paymentId) {
        // AuthorizedPayment is distinct from Payment V1, but the Java SDK might not
        // expose it directly.
        // We attempt to fetch using PaymentClient as a fallback, which sometimes
        // resolves authorized payments.

        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));
            return Optional.ofNullable(convertPaymentToMap(payment));
        } catch (Exception e) {
            // Si falla como Payment, tal vez es porque es AuthorizedPayment específico y no
            // está en Payments V1 aun?
            // Logueamos y retornamos empty.
            log.warn(
                    "Error fetching AuthorizedPayment as Payment SDK ({}). Esto puede ser esperado si el recurso es distinto. {}",
                    paymentId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Map<String, Object>> getPreapprovalPlan(String mpPlanId) {
        // SDK Java 2.1.29 no tiene PreapprovalPlanClient expuesto fácilmente.
        // Usamos RestTemplate localmente para este endpoint específico.
        try {
            String url = "https://api.mercadopago.com/preapproval_plan/" + mpPlanId;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<String, Object>> mapTypeRef = new ParameterizedTypeReference<>() {
            };
            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    mapTypeRef);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return Optional.of(body);
            }
        } catch (Exception e) {
            log.error("Error fetching Plan Manual ({}): {}", mpPlanId, e.getMessage());
        }
        return Optional.empty();
    }

    // Helpers básicos para convertir objetos SDK a Map y no romper el contrato de
    // MercadoPagoPort
    // idealmente se debería refactorizar el Port para devolver objetos tipados,
    // pero eso implica cambiar toda la app.
    private Map<String, Object> convertPreapprovalToMap(Preapproval p) {
        if (p == null)
            return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("status", p.getStatus());
        map.put("external_reference", p.getExternalReference());
        map.put("payer_id", p.getPayerId());
        // Agregar más campos si son necesarios en la app
        return map;
    }

    private Map<String, Object> convertPaymentToMap(Payment p) {
        if (p == null)
            return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("status", p.getStatus());
        map.put("status_detail", p.getStatusDetail());
        map.put("external_reference", p.getExternalReference());
        return map;
    }
}
