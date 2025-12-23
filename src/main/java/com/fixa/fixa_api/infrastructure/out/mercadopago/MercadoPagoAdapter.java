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
        // En producción, el API POST /preapproval con preapproval_plan_id requiere
        // card_token_id.
        // Por lo tanto, para Hosted Checkout debemos usar el link manual.

        try {
            String encodedRef = java.net.URLEncoder.encode(userId + ":" + planId,
                    java.nio.charset.StandardCharsets.UTF_8);
            String encodedEmail = java.net.URLEncoder.encode(userEmail, java.nio.charset.StandardCharsets.UTF_8);
            String encodedBackUrl = java.net.URLEncoder.encode(backUrl, java.nio.charset.StandardCharsets.UTF_8);

            String checkoutUrl = "https://www.mercadopago.com.ar/subscriptions/checkout?preapproval_plan_id=" + mpPlanId
                    + "&payer_email=" + encodedEmail
                    + "&external_reference=" + encodedRef
                    + "&external_id=" + encodedRef
                    + "&client_id=" + encodedRef
                    + "&back_url=" + encodedBackUrl;

            log.info("Generando link de suscripción manual (URL Encoded) para usuario {}: {}", userId, checkoutUrl);
            return checkoutUrl;
        } catch (Exception e) {
            log.error("Error codificando URLs para link de suscripción: {}", e.getMessage());
            // Fallback sin codificar (riesgoso pero mejor que nada)
            return "https://www.mercadopago.com.ar/subscriptions/checkout?preapproval_plan_id=" + mpPlanId
                    + "&payer_email=" + userEmail
                    + "&external_reference=" + userId + ":" + planId
                    + "&back_url=" + backUrl;
        }
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
