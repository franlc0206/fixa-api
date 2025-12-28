package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mp_scheduled_notification")
@Data
public class MpScheduledNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", length = 100, unique = true)
    private String notificationId;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "topic", length = 50)
    private String topic;

    @Column(name = "status", length = 20)
    private String status; // PENDING, PROCESSED, FAILED

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_attempt")
    private LocalDateTime nextAttempt;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null)
            status = "PENDING";
        if (retryCount == null)
            retryCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
