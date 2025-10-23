package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.infrastructure.in.web.dto.DisponibilidadRequest;
import com.fixa.fixa_api.application.service.DisponibilidadService;
import com.fixa.fixa_api.domain.model.Disponibilidad;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping("/api/empleados/{empleadoId}/disponibilidad")
    public ResponseEntity<List<Disponibilidad>> listar(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(disponibilidadService.listarPorEmpleado(empleadoId));
    }

    @PostMapping("/api/empleados/{empleadoId}/disponibilidad")
    public ResponseEntity<Disponibilidad> crear(@PathVariable Long empleadoId, @Valid @RequestBody DisponibilidadRequest req) {
        Disponibilidad d = new Disponibilidad();
        d.setEmpleadoId(empleadoId);
        d.setDiaSemana(req.getDiaSemana());
        d.setHoraInicio(LocalTime.parse(req.getHoraInicio()));
        d.setHoraFin(LocalTime.parse(req.getHoraFin()));
        return ResponseEntity.ok(disponibilidadService.guardar(d));
    }

    @DeleteMapping("/api/disponibilidad/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!disponibilidadService.eliminar(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
