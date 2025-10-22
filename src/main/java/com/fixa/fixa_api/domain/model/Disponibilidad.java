package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class Disponibilidad {
    private Long id;
    private Long empleadoId;
    private String diaSemana; // lunes..domingo
    private java.time.LocalTime horaInicio;
    private java.time.LocalTime horaFin;
}
