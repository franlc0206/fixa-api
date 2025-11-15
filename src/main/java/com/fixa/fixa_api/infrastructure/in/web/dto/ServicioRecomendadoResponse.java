package com.fixa.fixa_api.infrastructure.in.web.dto;

import com.fixa.fixa_api.application.service.RecomendacionService;

import java.math.BigDecimal;

public class ServicioRecomendadoResponse {
    private Long id;
    private Long empresaId;
    private String empresaNombre;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private double promedioValoracion;
    private long totalValoraciones;

    public static ServicioRecomendadoResponse fromDomain(RecomendacionService.ServicioRecomendado src) {
        ServicioRecomendadoResponse dto = new ServicioRecomendadoResponse();
        dto.setId(src.getId());
        dto.setEmpresaId(src.getEmpresaId());
        dto.setEmpresaNombre(src.getEmpresaNombre());
        dto.setNombre(src.getNombre());
        dto.setDescripcion(src.getDescripcion());
        dto.setDuracionMinutos(src.getDuracionMinutos());
        dto.setPrecio(src.getPrecio());
        dto.setPromedioValoracion(src.getPromedioValoracion());
        dto.setTotalValoraciones(src.getTotalValoraciones());
        return dto;
    }

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

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public double getPromedioValoracion() {
        return promedioValoracion;
    }

    public void setPromedioValoracion(double promedioValoracion) {
        this.promedioValoracion = promedioValoracion;
    }

    public long getTotalValoraciones() {
        return totalValoraciones;
    }

    public void setTotalValoraciones(long totalValoraciones) {
        this.totalValoraciones = totalValoraciones;
    }
}
