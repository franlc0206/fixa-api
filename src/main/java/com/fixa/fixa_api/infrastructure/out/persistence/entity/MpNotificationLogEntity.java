package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mp_notification_log")
@Data
public class MpNotificationLogEntity {
    @Id
    @Column(name = "notification_id", length = 100)
    private String notificationId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
}
