package com.fixa.fixa_api.domain.model;

public class ValoracionResumen {
    private Long empresaId;
    private double promedio;
    private long totalValoraciones;
    private long totalConResena;

    public ValoracionResumen(Long empresaId, double promedio, long totalValoraciones, long totalConResena) {
        this.empresaId = empresaId;
        this.promedio = promedio;
        this.totalValoraciones = totalValoraciones;
        this.totalConResena = totalConResena;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public double getPromedio() {
        return promedio;
    }

    public long getTotalValoraciones() {
        return totalValoraciones;
    }

    public long getTotalConResena() {
        return totalConResena;
    }

    public long getTotalSinResena() {
        return totalValoraciones - totalConResena;
    }
}
