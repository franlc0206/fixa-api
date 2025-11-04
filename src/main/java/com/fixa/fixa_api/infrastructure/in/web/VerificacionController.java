package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.usecase.ConfirmarCodigoUseCase;
import com.fixa.fixa_api.application.usecase.CrearVerificacionUseCase;
import com.fixa.fixa_api.domain.model.VerificacionTelefono;
import com.fixa.fixa_api.infrastructure.in.web.dto.VerificacionConfirmRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.VerificacionCreateRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.VerificacionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para verificación telefónica.
 * Siguiendo arquitectura hexagonal, este controller:
 * - Pertenece a la capa de infraestructura (web - adapter in)
 * - Expone endpoints REST públicos
 * - Mapea DTOs a modelos de dominio
 * - Delega la lógica de negocio a use cases
 * - No contiene lógica de negocio, solo orchestration
 */
@RestController
@RequestMapping("/api/public/verificaciones")
public class VerificacionController {

    private final CrearVerificacionUseCase crearVerificacionUseCase;
    private final ConfirmarCodigoUseCase confirmarCodigoUseCase;

    public VerificacionController(
            CrearVerificacionUseCase crearVerificacionUseCase,
            ConfirmarCodigoUseCase confirmarCodigoUseCase) {
        this.crearVerificacionUseCase = crearVerificacionUseCase;
        this.confirmarCodigoUseCase = confirmarCodigoUseCase;
    }

    /**
     * POST /api/public/verificaciones
     * Crea una verificación telefónica y envía el código por SMS/WhatsApp.
     * 
     * @param request Datos de la verificación (teléfono, canal, turnoId opcional)
     * @return Verificación creada con información del envío
     */
    @PostMapping
    public ResponseEntity<VerificacionResponse> crear(@Valid @RequestBody VerificacionCreateRequest request) {
        // Mapear DTO a parámetros del use case
        VerificacionTelefono verificacion = crearVerificacionUseCase.ejecutar(
                request.getTelefono(),
                request.getCanal(),
                request.getTurnoId()
        );

        // Mapear modelo de dominio a DTO de respuesta
        VerificacionResponse response = new VerificacionResponse();
        response.setId(verificacion.getId());
        response.setTelefono(verificacion.getTelefono());
        response.setCanal(verificacion.getCanal());
        response.setFechaEnvio(verificacion.getFechaEnvio());
        response.setFechaExpiracion(verificacion.getFechaExpiracion());
        response.setValidado(verificacion.isValidado());
        response.setTurnoId(verificacion.getTurnoId());
        response.setMessage("Código de verificación enviado por " + verificacion.getCanal() + 
                          ". Válido por 5 minutos.");

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/public/verificaciones/{id}/confirm
     * Confirma un código de verificación.
     * 
     * @param id ID de la verificación
     * @param request Código ingresado por el usuario
     * @return Verificación confirmada
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<VerificacionResponse> confirmar(
            @PathVariable Long id,
            @Valid @RequestBody VerificacionConfirmRequest request) {
        
        // Ejecutar use case
        VerificacionTelefono verificacion = confirmarCodigoUseCase.ejecutar(id, request.getCodigo());

        // Mapear modelo de dominio a DTO de respuesta
        VerificacionResponse response = new VerificacionResponse();
        response.setId(verificacion.getId());
        response.setTelefono(verificacion.getTelefono());
        response.setCanal(verificacion.getCanal());
        response.setFechaEnvio(verificacion.getFechaEnvio());
        response.setFechaExpiracion(verificacion.getFechaExpiracion());
        response.setValidado(verificacion.isValidado());
        response.setTurnoId(verificacion.getTurnoId());
        response.setMessage(verificacion.getTurnoId() != null 
                ? "Código verificado exitosamente. Tu turno ha sido confirmado." 
                : "Código verificado exitosamente.");

        return ResponseEntity.ok(response);
    }
}
