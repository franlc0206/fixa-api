package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmpresaRequest {
    @NotBlank
    private String nombre;
    private String descripcion;
    private String direccion;
    private String telefono;
    @Email
    private String email;
    private Long categoriaId; // opcional
    private boolean permiteReservasSinUsuario;
    private boolean requiereValidacionTelefono;
    private boolean requiereAprobacionTurno;
    private String mensajeValidacionPersonalizado;
    private boolean visibilidadPublica;
    private boolean activo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public boolean isPermiteReservasSinUsuario() { return permiteReservasSinUsuario; }
    public void setPermiteReservasSinUsuario(boolean permiteReservasSinUsuario) { this.permiteReservasSinUsuario = permiteReservasSinUsuario; }
    public boolean isRequiereValidacionTelefono() { return requiereValidacionTelefono; }
    public void setRequiereValidacionTelefono(boolean requiereValidacionTelefono) { this.requiereValidacionTelefono = requiereValidacionTelefono; }
    public boolean isRequiereAprobacionTurno() { return requiereAprobacionTurno; }
    public void setRequiereAprobacionTurno(boolean requiereAprobacionTurno) { this.requiereAprobacionTurno = requiereAprobacionTurno; }
    public String getMensajeValidacionPersonalizado() { return mensajeValidacionPersonalizado; }
    public void setMensajeValidacionPersonalizado(String mensajeValidacionPersonalizado) { this.mensajeValidacionPersonalizado = mensajeValidacionPersonalizado; }
    public boolean isVisibilidadPublica() { return visibilidadPublica; }
    public void setVisibilidadPublica(boolean visibilidadPublica) { this.visibilidadPublica = visibilidadPublica; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
