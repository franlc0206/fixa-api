package com.fixa.fixa_api.domain.model;

import java.time.LocalDateTime;

public class UsuarioOnboardingProgreso {
    private Long id;
    private Long usuarioId;
    private String featureKey;
    private Boolean completado;
    private Integer pasoActual;

    public UsuarioOnboardingProgreso(Long id, Long usuarioId, String featureKey, Boolean completado, Integer pasoActual,
            LocalDateTime fechaCompletado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.featureKey = featureKey;
        this.completado = completado;
        this.pasoActual = pasoActual;
        this.fechaCompletado = fechaCompletado;
    }

    public UsuarioOnboardingProgreso() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public Boolean getCompletado() {
        return completado;
    }

    public void setCompletado(Boolean completado) {
        this.completado = completado;
    }

    public Integer getPasoActual() {
        return pasoActual;
    }

    public void setPasoActual(Integer pasoActual) {
        this.pasoActual = pasoActual;
    }

    public LocalDateTime getFechaCompletado() {
        return fechaCompletado;
    }

    public void setFechaCompletado(LocalDateTime fechaCompletado) {
        this.fechaCompletado = fechaCompletado;
    }
}
