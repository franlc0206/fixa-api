package com.fixa.fixa_api.application.usecase;

import com.fixa.fixa_api.domain.model.VerificacionTelefono;

/**
 * Use case: Crear una verificación telefónica y enviar el código por SMS/WhatsApp.
 * Siguiendo arquitectura hexagonal, este use case solo conoce puertos del dominio.
 */
public interface CrearVerificacionUseCase {
    /**
     * Crea una verificación telefónica, genera un código aleatorio y lo envía.
     * 
     * @param telefono Número de teléfono en formato internacional
     * @param canal Canal de envío (sms o whatsapp)
     * @param turnoId ID del turno asociado (opcional, puede ser null)
     * @return VerificacionTelefono creada con el código generado
     */
    VerificacionTelefono ejecutar(String telefono, String canal, Long turnoId);
}
