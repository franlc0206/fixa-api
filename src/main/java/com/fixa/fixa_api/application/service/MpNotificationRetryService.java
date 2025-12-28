package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.MpNotificationLogEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.MpScheduledNotificationEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.MpNotificationLogJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.MpScheduledNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MpNotificationRetryService {

    private static final Logger log = LoggerFactory.getLogger(MpNotificationRetryService.class);

    private final MpScheduledNotificationRepository scheduledRepo;
    private final MpNotificationLogJpaRepository rawLogRepo;
    private final MercadoPagoSuscripcionService suscripcionService;

    public MpNotificationRetryService(MpScheduledNotificationRepository scheduledRepo,
            MpNotificationLogJpaRepository rawLogRepo,
            MercadoPagoSuscripcionService suscripcionService) {
        this.scheduledRepo = scheduledRepo;
        this.rawLogRepo = rawLogRepo;
        this.suscripcionService = suscripcionService;
    }

    /**
     * Reintenta todas las notificaciones pendientes o fallidas que no hayan
     * superado el máximo de intentos.
     */
    public void retryAllPending() {
        List<MpScheduledNotificationEntity> pending = scheduledRepo.findPendingForRetry(10, LocalDateTime.now());
        if (pending.isEmpty()) {
            log.info("No hay notificaciones pendientes para reintentar.");
            return;
        }
        log.info("Iniciando reintento manual de {} notificaciones.", pending.size());
        pending.forEach(this::procesarReintento);
    }

    /**
     * Reintenta una notificación específica por su ID de Mercado Pago.
     */
    public void retrySpecific(String notificationId) {
        scheduledRepo.findByNotificationId(notificationId).ifPresent(this::procesarReintento);
    }

    @Transactional
    public void procesarReintento(MpScheduledNotificationEntity notif) {
        log.info("Reintentando notificación {} (Intento {})...", notif.getNotificationId(), notif.getRetryCount() + 1);
        try {
            // Intentamos procesar de nuevo
            suscripcionService.ejecutarRuteoNotificacion(notif.getTopic(), notif.getResourceId(), null);

            // Éxito: marcar como procesado
            notif.setStatus("PROCESSED");
            notif.setLastError(null);
            scheduledRepo.save(notif);

            // También guardamos en el log de idempotencia para evitar duplicados si el
            // webhook original llegara tarde
            if (!rawLogRepo.existsById(notif.getNotificationId())) {
                MpNotificationLogEntity logEntry = new MpNotificationLogEntity();
                logEntry.setNotificationId(notif.getNotificationId());
                logEntry.setProcessedAt(LocalDateTime.now());
                logEntry.setPayload(notif.getPayload());
                rawLogRepo.save(logEntry);
            }

            log.info("Notificación {} reintentada con éxito.", notif.getNotificationId());

        } catch (Exception e) {
            log.error("Fallo reintento de notificación {}: {}", notif.getNotificationId(), e.getMessage());
            notif.setStatus("FAILED");
            notif.setLastError(e.getMessage());
            notif.setRetryCount(notif.getRetryCount() + 1);

            // Simplemente actualizamos el registro para auditoría
            scheduledRepo.save(notif);
        }
    }
}
