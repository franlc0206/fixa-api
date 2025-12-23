package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.application.service.MpNotificationRetryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/superadmin/mercadopago")
public class SuperAdminMercadoPagoController {

    private final MpNotificationRetryService retryService;

    public SuperAdminMercadoPagoController(MpNotificationRetryService retryService) {
        this.retryService = retryService;
    }

    /**
     * Reintenta todas las notificaciones pendientes o fallidas.
     */
    @PostMapping("/notifications/retry-all")
    public ResponseEntity<Map<String, String>> retryAll() {
        retryService.retryAllPending();
        return ResponseEntity.ok(Map.of("message", "Proceso de reintento de todas las notificaciones iniciado."));
    }

    /**
     * Reintenta una notificación específica por su ID de Mercado Pago.
     */
    @PostMapping("/notifications/{id}/retry")
    public ResponseEntity<Map<String, String>> retrySpecific(@PathVariable String id) {
        retryService.retrySpecific(id);
        return ResponseEntity.ok(Map.of("message", "Reintento de notificación " + id + " procesado."));
    }
}
