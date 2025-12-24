package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.MercadoPagoSuscripcionService;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MercadoPagoController {

    private final MercadoPagoSuscripcionService mpService;
    private final CurrentUserService currentUserService;

    public MercadoPagoController(MercadoPagoSuscripcionService mpService, CurrentUserService currentUserService) {
        this.mpService = mpService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/backoffice/mercadopago/init")
    public ResponseEntity<Map<String, String>> iniciarSuscripcion(@RequestParam Long planId,
            @RequestParam(required = false) String payerEmail) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));

        String url = mpService.iniciarSuscripcion(userId, planId, payerEmail);
        return ResponseEntity.ok(Map.of("checkoutUrl", url));
    }

    @PostMapping("/public/mercadopago/webhook")
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestParam Map<String, String> allParams,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestHeader(value = "x-request-id", required = false) String requestId) {

        mpService.procesarWebhook(payload, allParams, signature, requestId);
        return ResponseEntity.ok().build();
    }
}
