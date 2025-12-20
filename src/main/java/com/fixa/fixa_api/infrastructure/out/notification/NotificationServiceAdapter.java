package com.fixa.fixa_api.infrastructure.out.notification;

import com.fixa.fixa_api.domain.service.NotificationServicePort;
import com.fixa.fixa_api.infrastructure.out.notification.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class NotificationServiceAdapter implements NotificationServicePort {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceAdapter.class);
    private final RestTemplate restTemplate;

    @Value("${notifications.api.url:https://beta.notificaciones.fixe.com.ar/api/notifications}")
    private String apiUrl;

    @Value("${notifications.api.key:}")
    private String apiKey;

    public NotificationServiceAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendEmail(String to, String template, Map<String, String> variables) {
        send("EMAIL", to, template, variables);
    }

    @Override
    public void sendWhatsApp(String to, String template, Map<String, String> variables) {
        send("WHATSAPP", to, template, variables);
    }

    private void send(String channel, String to, String template, Map<String, String> variables) {
        if (to == null || to.isBlank()) {
            log.warn("Intento de envío de notificación sin destinatario para el canal {}", channel);
            return;
        }

        NotificationRequest request = NotificationRequest.builder()
                .channel(channel)
                .to(to)
                .template(template)
                .variables(variables)
                .build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", apiKey);

            HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

            log.info("Enviando notificación {} a {}. Template: {}", channel, to, template);

            // Si la API key no está configurada, solo logueamos para evitar fallos en dev
            if (apiKey == null || apiKey.isBlank()) {
                log.warn("APP_API_KEY no configurada. La notificación no se enviará realmente (MOCK).");
                return;
            }

            restTemplate.postForEntity(apiUrl, entity, String.class);
            log.info("Notificación enviada exitosamente");

        } catch (Exception e) {
            log.error("Error al enviar notificación {}: {}", channel, e.getMessage());
            // No lanzamos excepción para no romper el flujo de negocio principal (turnos)
        }
    }
}
