package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public class DisponibilidadRequest {
    @NotBlank
    private String diaSemana; // LUNES, MARTES, etc.
    @NotBlank
    private String horaInicio; // HH:mm
    @NotBlank
    private String horaFin; // HH:mm

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
}
