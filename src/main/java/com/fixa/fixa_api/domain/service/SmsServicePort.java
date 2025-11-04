package com.fixa.fixa_api.domain.service;

/**
 * Puerto (interface) para el servicio de envío de SMS.
 * Siguiendo arquitectura hexagonal, esta interface pertenece al dominio
 * y será implementada por un adapter en la capa de infraestructura (ej: Twilio).
 * 
 * El dominio no debe conocer detalles de implementación de Twilio u otros servicios externos.
 */
public interface SmsServicePort {
    /**
     * Envía un SMS con el código de verificación.
     * 
     * @param telefono Número de teléfono en formato internacional (ej: +5491112345678)
     * @param codigo Código de verificación a enviar
     * @param canal Canal de envío (sms o whatsapp)
     * @return true si el envío fue exitoso, false en caso contrario
     */
    boolean enviarCodigoVerificacion(String telefono, String codigo, String canal);
}
