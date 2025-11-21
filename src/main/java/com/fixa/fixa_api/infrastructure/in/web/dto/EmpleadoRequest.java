package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public class EmpleadoRequest {
    // empresaId es opcional en el body porque puede venir del @PathVariable en endpoints como POST /api/empresas/{empresaId}/empleados
    private Long empresaId;
    
    @NotBlank
    private String nombre;
    
    @NotBlank
    private String apellido;
    
    private String email;

    private String rol;
    private String fotoUrl;
    private boolean trabajaPublicamente = true;
    private boolean activo = true;

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public boolean isTrabajaPublicamente() { return trabajaPublicamente; }
    public void setTrabajaPublicamente(boolean trabajaPublicamente) { this.trabajaPublicamente = trabajaPublicamente; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
