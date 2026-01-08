package com.fixa.fixa_api.infrastructure.out.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class NotificationClient {

    private final RestClient restClient;
    private final String notificationApiUrl;
    private final String apiKey;

    public NotificationClient(@Value("${NOTIFICATION_API_URL:http://localhost:8081}") String notificationApiUrl,
            @Value("${NOTIFICATION_API_KEY:}") String apiKey) {
        this.notificationApiUrl = notificationApiUrl;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(notificationApiUrl)
                .build();
    }

    public void sendWelcomeEmail(String email, String name) {
        Map<String, Object> payload = Map.of(
                "type", "EMAIL",
                "template", "welcome_onboarding",
                "target", email,
                "data", Map.of("name", name));

        try {
            restClient.post()
                    .uri("/api/v1/notifications")
                    .header("X-API-KEY", apiKey)
                    .body(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        System.err.println("Error sending notification: " + response.getStatusCode());
                    })
                    .toBodilessEntity();
        } catch (Exception e) {
            // Log error but don't block main flow
            System.err.println("Failed to send welcome notification: " + e.getMessage());
        }
    }
}
