package com.fixa.fixa_api.domain.service;

import java.util.Map;

public interface NotificationServicePort {
    void sendEmail(String to, String template, Map<String, String> variables);

    void sendWhatsApp(String to, String template, Map<String, String> variables);
}
