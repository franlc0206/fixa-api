package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.ValoracionService;
import com.fixa.fixa_api.domain.model.Valoracion;
import com.fixa.fixa_api.infrastructure.in.web.dto.ValoracionRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.ValoracionResponse;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ValoracionController {

    private final ValoracionService valoracionService;
    private final CurrentUserService currentUserService;

    public ValoracionController(ValoracionService valoracionService,
                                CurrentUserService currentUserService) {
        this.valoracionService = valoracionService;
        this.currentUserService = currentUserService;
    }

    /**
     * Crear una nueva valoración (requiere autenticación)
     * Solo usuarios registrados con turnos completados pueden valorar
     */
    @PostMapping("/me/valoraciones")
    public ResponseEntity<ValoracionResponse> crearValoracion(
            @Valid @RequestBody ValoracionRequest request) {

        Long usuarioId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        
        // Crear valoración
        Valoracion valoracion = new Valoracion();
        valoracion.setUsuarioId(usuarioId);
        valoracion.setTurnoId(request.getTurnoId());
        valoracion.setEmpresaId(request.getEmpresaId());
        valoracion.setPuntuacion(request.getPuntuacion());
        valoracion.setResena(request.getResena());
        
        Valoracion creada = valoracionService.crearValoracion(valoracion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ValoracionResponse.fromDomain(creada));
    }

    /**
     * Obtener valoraciones del usuario autenticado
     */
    @GetMapping("/me/valoraciones")
    public ResponseEntity<List<ValoracionResponse>> obtenerMisValoraciones() {
        Long usuarioId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        
        List<Valoracion> valoraciones = valoracionService.obtenerValoracionesPorUsuario(usuarioId);
        List<ValoracionResponse> response = valoraciones.stream()
                .map(ValoracionResponse::fromDomain)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener una valoración por ID
     */
    @GetMapping("/valoraciones/{id}")
    public ResponseEntity<ValoracionResponse> obtenerValoracion(@PathVariable Long id) {
        Valoracion valoracion = valoracionService.obtenerValoracionPorId(id);
        return ResponseEntity.ok(ValoracionResponse.fromDomain(valoracion));
    }

    /**
     * Obtener valoración de un turno específico
     */
    @GetMapping("/valoraciones/turnos/{turnoId}")
    public ResponseEntity<ValoracionResponse> obtenerValoracionPorTurno(@PathVariable Long turnoId) {
        Valoracion valoracion = valoracionService.obtenerValoracionPorTurno(turnoId);
        return ResponseEntity.ok(ValoracionResponse.fromDomain(valoracion));
    }

    /**
     * Desactivar una valoración (solo el autor puede hacerlo)
     */
    @DeleteMapping("/valoraciones/{id}")
    public ResponseEntity<Void> desactivarValoracion(@PathVariable Long id) {

        Long usuarioId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        valoracionService.desactivarValoracion(id, usuarioId);

        return ResponseEntity.noContent().build();
    }
}
