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
        // INTENTO 1: Crear vía API POST /preapproval (Recomendado para persistir
        // external_reference)
        try {
            String externalRef = userId + ":" + planId;
            String computedBackUrl = (backUrl != null && !backUrl.isBlank()) ? backUrl : "https://fixe.com.ar";

            // Construir JSON body
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("preapproval_plan_id", mpPlanId);
            // body.put("payer_email", userEmail); // Comentado para evitar error
            // card_token_id is required
            body.put("external_reference", externalRef);
            body.put("back_url", computedBackUrl);
            body.put("status", "pending"); // Status pending genera el init_point para checkout

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("Intentando crear Preapproval vía API para usuario {} (Plan {})...", userId, mpPlanId);

            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(
                    "https://api.mercadopago.com/preapproval",
                    entity,
                    java.util.Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String initPoint = (String) response.getBody().get("init_point");
                if (initPoint != null && !initPoint.isBlank()) {
                    log.info("Preapproval API exitoso. Redirigiendo a: {}", initPoint);
                    return initPoint;
                }
            }
        } catch (Exception e) {
            log.error("Fallo creación API Preapproval: {}", e.getMessage(), e);
            throw new RuntimeException("Error creando suscripción en Mercado Pago: " + e.getMessage(), e);
        }

        throw new RuntimeException("No se pudo obtener el link de suscripción de la respuesta de Mercado Pago.");
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

    @Override
    public Optional<Map<String, Object>> getPreapprovalPlan(String mpPlanId) {
        String url = "https://api.mercadopago.com/preapproval_plan/" + mpPlanId;
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
