package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServicioRequest {
    // empresaId es opcional en el body porque puede venir del @PathVariable en endpoints como POST /api/empresas/{empresaId}/servicios
    private Long empresaId;
    
    @NotBlank
    private String nombre;
    
    private String descripcion;
    
    @NotNull
    private Integer duracionMinutos;
    
    private boolean requiereEspacioLibre;
    
    @NotNull
    private BigDecimal costo;
    
    private boolean requiereSena;
    private boolean activo = true;
    private Long categoriaId; // opcional
    private String fotoUrl;

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public boolean isRequiereEspacioLibre() { return requiereEspacioLibre; }
    public void setRequiereEspacioLibre(boolean requiereEspacioLibre) { this.requiereEspacioLibre = requiereEspacioLibre; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
    public boolean isRequiereSena() { return requiereSena; }
    public void setRequiereSena(boolean requiereSena) { this.requiereSena = requiereSena; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
