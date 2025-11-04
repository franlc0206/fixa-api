package com.fixa.fixa_api.application.usecase;

import com.fixa.fixa_api.domain.model.VerificacionTelefono;

/**
 * Use case: Confirmar un código de verificación telefónica.
 * Siguiendo arquitectura hexagonal, este use case solo conoce puertos del dominio.
 */
public interface ConfirmarCodigoUseCase {
    /**
     * Confirma un código de verificación y actualiza el estado del turno asociado si corresponde.
     * 
     * @param verificacionId ID de la verificación a confirmar
     * @param codigo Código ingresado por el usuario
     * @return VerificacionTelefono validada
     * @throws com.fixa.fixa_api.infrastructure.in.web.error.ApiException si el código es inválido, expiró o la verificación no existe
     */
    VerificacionTelefono ejecutar(Long verificacionId, String codigo);
}
