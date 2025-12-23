package com.fixa.fixa_api.infrastructure.out.mercadopago;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAdapter.class);
    private final RestTemplate restTemplate;

    @Value("${mercadopago.access-token:}")
    private String accessToken;

    @Value("${mercadopago.back-url:https://fixe.com.ar/backoffice/suscripcion/ready}")
    private String backUrl;

    public MercadoPagoAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String createPreapprovalLink(String userEmail, Long userId, Long planId, String mpPlanId) {
        // En lugar de construir el link manual, usamos el API /preapproval para obtener
        // un init_point
        // que preserve el external_reference de forma robusta.
        String url = "https://api.mercadopago.com/preapproval";

        Map<String, Object> request = new java.util.HashMap<>();
        request.put("preapproval_plan_id", mpPlanId);
        request.put("payer_email", userEmail);
        request.put("external_reference", userId + ":" + planId);
        request.put("back_url", backUrl);
        request.put("status", "pending");
        request.put("metadata", Map.of(
                "user_id", userId,
                "plan_id", planId));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    new org.springframework.core.ParameterizedTypeReference<>() {
                    });

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String initPoint = (String) response.getBody().get("init_point");
                if (initPoint != null) {
                    log.info("Link de suscripción (init_point) generado exitosamente para usuario {}: {}", userId,
                            initPoint);
                    return initPoint;
                }
            }
            log.error("Respuesta inesperada de MP al crear preapproval: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error llamando a la API de MP para crear link: {}", e.getMessage());
        }

        // Fallback al sistema anterior solo si falla la API, aunque es menos robusto
        log.warn("Error usando API de MP, usando link manual como fallback.");
        return "https://www.mercadopago.com.ar/subscriptions/checkout?preapproval_plan_id=" + mpPlanId
                + "&payer_email=" + userEmail
                + "&external_reference=" + userId + ":" + planId
                + "&back_url=" + backUrl;
    }

    @Override
    public Optional<Map<String, Object>> getPreapproval(String preapprovalId) {
        String url = "https://api.mercadopago.com/preapproval/" + preapprovalId;
        return executeGet(url);
    }

    @Override
    public Optional<Map<String, Object>> getPayment(String paymentId) {
        String url = "https://api.mercadopago.com/v1/payments/" + paymentId;
        return executeGet(url);
    }

    @Override
    public Optional<Map<String, Object>> getAuthorizedPayment(String paymentId) {
        // Los pagos de suscripciones se consultan en este endpoint específico
        String url = "https://api.mercadopago.com/authorized_payments/" + paymentId;
        return executeGet(url);
    }

    private Optional<Map<String, Object>> executeGet(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            org.springframework.core.ParameterizedTypeReference<Map<String, Object>> typeRef = new org.springframework.core.ParameterizedTypeReference<>() {
            };
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error fetching data from MP ({}): {}", url, e.getMessage());
        }
        return Optional.empty();
    }
}
