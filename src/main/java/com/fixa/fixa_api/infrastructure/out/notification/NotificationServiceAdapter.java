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
        String processedContent = replaceVariables(template, variables);
        String htmlContent = wrapInHtmlTemplate(processedContent);
        send("EMAIL", to, htmlContent, variables);
    }

    @Override
    public void sendWhatsApp(String to, String template, Map<String, String> variables) {
        // Para WhatsApp no enviamos HTML, solo el texto procesado
        String processedContent = replaceVariables(template, variables);
        send("WHATSAPP", to, processedContent, variables);
    }

    private String replaceVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null)
            return template;
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    private String wrapInHtmlTemplate(String content) {
        // Colores extraídos del logo/brand: Blue (#2B3A8B), Coral (#EE6B61)
        return "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap');" +
                "  body { font-family: 'Inter', Arial, sans-serif; line-height: 1.6; color: #1a202c; margin: 0; padding: 0; background-color: #f7fafc; }"
                +
                "  .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(43, 58, 139, 0.1); border: 1px solid #edf2f7; }"
                +
                "  .header { background-color: #ffffff; padding: 40px 30px; text-align: center; border-bottom: 4px solid #EE6B61; }"
                +
                "  .logo { font-size: 32px; font-weight: 800; color: #2B3A8B; letter-spacing: -1px; text-decoration: none; }"
                +
                "  .logo span { color: #EE6B61; }" +
                "  .main-content { padding: 50px 40px; background-color: #ffffff; }" +
                "  .message { font-size: 17px; color: #4a5568; line-height: 1.8; }" +
                "  .footer { background: #2B3A8B; padding: 30px; text-align: center; font-size: 13px; color: #ffffff; }"
                +
                "  .footer b { color: #ffffff; }" +
                "  b { color: #2B3A8B; font-weight: 700; }" +
                "</style></head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'><div class='logo'>Fix<span>e</span></div></div>" +
                "    <div class='main-content'><div class='message'>" +
                content.replace("\n", "<br>") +
                "    </div></div>" +
                "    <div class='footer'>" +
                "      &copy; 2025 <b>Fixe</b> - Potenciando tu negocio<br>" +
                "      <div style='margin-top: 10px; opacity: 0.8;'>Este es un mensaje automático, por favor no lo respondas.</div>"
                +
                "    </div>" +
                "  </div>" +
                "</body></html>";
    }

    private void send(String channel, String to, String content, Map<String, String> variables) {
        if (to == null || to.isBlank()) {
            log.warn("Intento de envío de notificación sin destinatario para el canal {}", channel);
            return;
        }

        NotificationRequest request = NotificationRequest.builder()
                .channel(channel)
                .to(to)
                .template(content) // Ahora enviamos el contenido ya procesado/HTML como "template"
                .variables(variables) // Mantenemos variables por compatibilidad futura de la API
                .build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", apiKey);

            HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

            log.info("Enviando notificación {} a {}.", channel, to);

            if (apiKey == null || apiKey.isBlank()) {
                log.warn("APP_API_KEY no configurada. La notificación no se enviará realmente (MOCK).");
                return;
            }

            restTemplate.postForEntity(apiUrl, entity, String.class);
            log.info("Notificación enviada exitosamente");

        } catch (Exception e) {
            log.error("Error al enviar notificación {}: {}", channel, e.getMessage());
        }
    }
}
