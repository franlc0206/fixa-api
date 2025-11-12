package com.fixa.fixa_api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa un slot específico de disponibilidad en una fecha concreta.
 * Se genera a partir de la configuración de Disponibilidad (por día de semana).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadSlot {
    private Long id;
    private Long empleadoId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean disponible;
}
