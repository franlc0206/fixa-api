package com.fixa.fixa_api.infrastructure.in.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class DisponibilidadGlobalSlot {
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private List<EmpleadoResumen> empleadosDisponibles;

    @Data
    @Builder
    public static class EmpleadoResumen {
        private Long id;
        private String nombre;
        private String apellido;
        private String fotoUrl;
    }
}
