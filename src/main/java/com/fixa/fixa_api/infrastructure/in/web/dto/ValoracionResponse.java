package com.fixa.fixa_api.infrastructure.in.web.dto;

import com.fixa.fixa_api.domain.model.Valoracion;
import java.time.LocalDateTime;

public class ValoracionResponse {
    private Long id;
    private Long empresaId;
    private Long usuarioId;
    private String usuarioNombre;
    private Long turnoId;
    private Integer puntuacion;
    private String resena;
    private LocalDateTime fechaCreacion;
    private boolean activo;

    public static ValoracionResponse fromDomain(Valoracion v) {
        ValoracionResponse dto = new ValoracionResponse();
        dto.setId(v.getId());
        dto.setEmpresaId(v.getEmpresaId());
        dto.setUsuarioId(v.getUsuarioId());
        dto.setTurnoId(v.getTurnoId());
        dto.setPuntuacion(v.getPuntuacion());
        dto.setResena(v.getResena());
        dto.setFechaCreacion(v.getFechaCreacion());
        dto.setActivo(v.isActivo());
        return dto;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public Long getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(Long turnoId) {
        this.turnoId = turnoId;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
