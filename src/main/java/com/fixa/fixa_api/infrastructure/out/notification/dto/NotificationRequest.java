package com.fixa.fixa_api.infrastructure.out.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class NotificationRequest {
    private String channel;
    private String to;
    private String template;
    private Map<String, String> variables;
}
