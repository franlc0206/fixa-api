package com.fixa.fixa_api.infrastructure.in.web.dto;

import com.fixa.fixa_api.domain.model.Valoracion;

import java.time.LocalDateTime;

public class ValoracionPublicaResponse {
    private Long id;
    private Integer puntuacion;
    private String resena;
    private LocalDateTime fechaCreacion;

    public static ValoracionPublicaResponse fromDomain(Valoracion valoracion) {
        ValoracionPublicaResponse dto = new ValoracionPublicaResponse();
        dto.setId(valoracion.getId());
        dto.setPuntuacion(valoracion.getPuntuacion());
        dto.setResena(valoracion.getResena());
        dto.setFechaCreacion(valoracion.getFechaCreacion());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getResena() {
        return resena;
    }

    public void setResena(String resena) {
        this.resena = resena;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
