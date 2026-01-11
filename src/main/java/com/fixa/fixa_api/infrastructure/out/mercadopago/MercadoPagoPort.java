package com.fixa.fixa_api.infrastructure.out.mercadopago;

import java.util.Map;
import java.util.Optional;

public interface MercadoPagoPort {
    String createPreapprovalLink(String userEmail, Long userId, Long planId, String mpPlanId);

    String createPreapprovalLink(String userEmail, Long userId, Long planId, String mpPlanId, String externalReference);

    Optional<Map<String, Object>> getPreapproval(String preapprovalId);

    Optional<Map<String, Object>> getPayment(String paymentId);

    Optional<Map<String, Object>> getAuthorizedPayment(String paymentId);

    Optional<Map<String, Object>> getPreapprovalPlan(String mpPlanId);
}
