package com.fixa.fixa_api.infrastructure.in.web.dto;

import com.fixa.fixa_api.application.service.RankingEmpresaService;

public class EmpresaDestacadaResponse {
    private Long id;
    private String nombre;
    private String slug;
    private String descripcion;
    private String telefono;
    private String email;
    private String logoUrl;
    private Long categoriaId;
    private double promedioValoracion;
    private long totalValoraciones;

    public static EmpresaDestacadaResponse fromDomain(RankingEmpresaService.EmpresaDestacada src) {
        EmpresaDestacadaResponse dto = new EmpresaDestacadaResponse();
        dto.setId(src.getId());
        dto.setNombre(src.getNombre());
        dto.setSlug(src.getSlug());
        dto.setDescripcion(src.getDescripcion());
        dto.setTelefono(src.getTelefono());
        dto.setEmail(src.getEmail());
        dto.setLogoUrl(src.getLogoUrl());
        dto.setCategoriaId(src.getCategoriaId());
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
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
