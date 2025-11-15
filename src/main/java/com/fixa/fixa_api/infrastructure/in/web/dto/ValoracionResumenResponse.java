package com.fixa.fixa_api.infrastructure.in.web.dto;

import com.fixa.fixa_api.domain.model.ValoracionResumen;

public class ValoracionResumenResponse {
    private Long empresaId;
    private double promedio;
    private long totalValoraciones;
    private long totalConResena;
    private long totalSinResena;

    public static ValoracionResumenResponse fromDomain(ValoracionResumen resumen) {
        ValoracionResumenResponse dto = new ValoracionResumenResponse();
        dto.setEmpresaId(resumen.getEmpresaId());
        dto.setPromedio(resumen.getPromedio());
        dto.setTotalValoraciones(resumen.getTotalValoraciones());
        dto.setTotalConResena(resumen.getTotalConResena());
        dto.setTotalSinResena(resumen.getTotalSinResena());
        return dto;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public double getPromedio() {
        return promedio;
    }

    public void setPromedio(double promedio) {
        this.promedio = promedio;
    }

    public long getTotalValoraciones() {
        return totalValoraciones;
    }

    public void setTotalValoraciones(long totalValoraciones) {
        this.totalValoraciones = totalValoraciones;
    }

    public long getTotalConResena() {
        return totalConResena;
    }

    public void setTotalConResena(long totalConResena) {
        this.totalConResena = totalConResena;
    }

    public long getTotalSinResena() {
        return totalSinResena;
    }

    public void setTotalSinResena(long totalSinResena) {
        this.totalSinResena = totalSinResena;
    }
}
