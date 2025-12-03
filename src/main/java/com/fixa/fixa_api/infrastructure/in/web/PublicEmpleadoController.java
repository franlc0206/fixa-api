package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.DisponibilidadSlotService;
import com.fixa.fixa_api.domain.model.DisponibilidadSlot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/empleados")
public class PublicEmpleadoController {

    private final DisponibilidadSlotService disponibilidadSlotService;

    public PublicEmpleadoController(DisponibilidadSlotService disponibilidadSlotService) {
        this.disponibilidadSlotService = disponibilidadSlotService;
    }

    /**
     * Obtener disponibilidad pública de un empleado en formato de slots específicos
     * por fecha.
     * Endpoint público sin autenticación para reservas anónimas.
     * 
     * @param empleadoId ID del empleado
     * @return Lista de slots disponibles para los próximos 30 días
     */
    @GetMapping("/{empleadoId}/disponibilidad")
    public ResponseEntity<List<DisponibilidadSlot>> obtenerDisponibilidadPublica(
            @PathVariable Long empleadoId,
            @RequestParam(required = false) Long servicioId) {

        // Generar slots para los próximos 30 días
        List<DisponibilidadSlot> slots = disponibilidadSlotService.generarSlotsParaEmpleado(empleadoId);

        // Filtrar solo slots disponibles y futuros
        List<DisponibilidadSlot> slotsDisponibles = disponibilidadSlotService.filtrarSlotsDisponibles(slots);

        // Si se especifica servicio, filtrar por intervalos
        if (servicioId != null) {
            slotsDisponibles = disponibilidadSlotService.filtrarSlotsPorServicio(slotsDisponibles, servicioId);
        }

        return ResponseEntity.ok(slotsDisponibles);
    }
}
