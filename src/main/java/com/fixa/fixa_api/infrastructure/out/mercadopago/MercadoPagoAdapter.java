package com.fixa.fixa_api.infrastructure.out.mercadopago;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
        String url = "https://api.mercadopago.com/preapproval";

        Map<String, Object> body = new HashMap<>();
        body.put("preapproval_plan_id", mpPlanId);
        body.put("payer_email", userEmail);
        body.put("back_url", backUrl);
        body.put("reason", "Suscripción Fixe");
        body.put("external_reference", userId + ":" + planId);
        body.put("status", "pending"); // Obligatorio para generar init_point sin tarjeta

        try {
            log.info("Iniciando creacion de preapproval para usuario {}. Plan: {}. Body: {}", userId, mpPlanId, body);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            org.springframework.core.ParameterizedTypeReference<Map<String, Object>> typeRef = new org.springframework.core.ParameterizedTypeReference<>() {
            };

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);

            log.info("Respuesta de MP (Preapproval): Status={}, Body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String initPoint = (String) response.getBody().get("init_point");
                if (initPoint != null && !initPoint.isBlank()) {
                    log.info("Link de suscripcion creado exitosamente: {}", initPoint);
                    return initPoint;
                }
            }
        } catch (Exception e) {
            log.error("Error al crear preapproval via API: {}", e.getMessage());
            if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
                log.error("Cuerpo del error MP: {}",
                        ((org.springframework.web.client.HttpStatusCodeException) e).getResponseBodyAsString());
            }
        }

        // Fallback al metodo manual si falla la API (Nota: esto no disparara webhooks
        // correctamente en algunos flujos)
        String fallbackLink = "https://www.mercadopago.com.ar/subscriptions/checkout?preapproval_plan_id=" + mpPlanId
                + "&payer_email=" + userEmail
                + "&external_reference=" + userId + ":" + planId
                + "&back_url=" + backUrl;
        log.warn("Usando fallback manual para link de suscripcion: {}", fallbackLink);
        return fallbackLink;
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
