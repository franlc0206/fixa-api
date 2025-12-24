package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.MpScheduledNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MpScheduledNotificationRepository extends JpaRepository<MpScheduledNotificationEntity, Long> {

    Optional<MpScheduledNotificationEntity> findByNotificationId(String notificationId);

    @Query("SELECT n FROM MpScheduledNotificationEntity n WHERE n.status IN ('PENDING', 'FAILED') AND n.retryCount < :maxRetries AND (n.nextAttempt IS NULL OR n.nextAttempt <= :now)")
    List<MpScheduledNotificationEntity> findPendingForRetry(@Param("maxRetries") Integer maxRetries,
            @Param("now") LocalDateTime now);
}
