package com.fixa.fixa_api.infrastructure.in.web.dto;

import com.fixa.fixa_api.domain.model.Turno;

import java.time.LocalDateTime;

public class TurnoMeResponse {

    private Long id;
    private Long servicioId;
    private Long empleadoId;
    private Long empresaId;
    private Long clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteTelefono;
    private String clienteDni;
    private String clienteEmail;
    private boolean telefonoValidado;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String estado;
    private boolean requiereValidacion;
    private String observaciones;
    private boolean yaValorado;
    private String empresaDireccion;

    // Nombres enriched
    private String servicioNombre;
    private String empleadoNombre;
    private String empresaNombre;
    private String empresaLogoUrl;
    private java.math.BigDecimal precio;

    public static TurnoMeResponse fromDomain(Turno turno, boolean yaValorado) {
        TurnoMeResponse dto = new TurnoMeResponse();
        dto.setId(turno.getId());
        dto.setServicioId(turno.getServicioId());
        dto.setEmpleadoId(turno.getEmpleadoId());
        dto.setEmpresaId(turno.getEmpresaId());
        dto.setClienteId(turno.getClienteId());
        dto.setClienteNombre(turno.getClienteNombre());
        dto.setClienteApellido(turno.getClienteApellido());
        dto.setClienteTelefono(turno.getClienteTelefono());
        dto.setClienteDni(turno.getClienteDni());
        dto.setClienteEmail(turno.getClienteEmail());
        dto.setTelefonoValidado(turno.isTelefonoValidado());
        dto.setFechaHoraInicio(turno.getFechaHoraInicio());
        dto.setFechaHoraFin(turno.getFechaHoraFin());
        dto.setEstado(turno.getEstado());
        dto.setRequiereValidacion(turno.isRequiereValidacion());
        dto.setObservaciones(turno.getObservaciones());
        dto.setYaValorado(yaValorado);

        dto.setServicioNombre(turno.getServicioNombre());
        dto.setEmpleadoNombre(turno.getEmpleadoNombre());
        dto.setEmpresaNombre(turno.getEmpresaNombre());
        dto.setEmpresaLogoUrl(turno.getEmpresaLogoUrl());
        dto.setEmpresaDireccion(turno.getEmpresaDireccion());
        dto.setPrecio(turno.getPrecio());

        return dto;
    }

    // Getters and Setters
    public String getServicioNombre() {
        return servicioNombre;
    }

    public void setServicioNombre(String servicioNombre) {
        this.servicioNombre = servicioNombre;
    }

    public String getEmpleadoNombre() {
        return empleadoNombre;
    }

    public void setEmpleadoNombre(String empleadoNombre) {
        this.empleadoNombre = empleadoNombre;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public String getEmpresaLogoUrl() {
        return empresaLogoUrl;
    }

    public void setEmpresaLogoUrl(String empresaLogoUrl) {
        this.empresaLogoUrl = empresaLogoUrl;
    }

    public String getEmpresaDireccion() {
        return empresaDireccion;
    }

    public void setEmpresaDireccion(String empresaDireccion) {
        this.empresaDireccion = empresaDireccion;
    }

    public java.math.BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(java.math.BigDecimal precio) {
        this.precio = precio;
    }

    // ... existing getters setters ...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServicioId() {
        return servicioId;
    }

    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
    }

    public Long getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteApellido() {
        return clienteApellido;
    }

    public void setClienteApellido(String clienteApellido) {
        this.clienteApellido = clienteApellido;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(String clienteDni) {
        this.clienteDni = clienteDni;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public boolean isTelefonoValidado() {
        return telefonoValidado;
    }

    public void setTelefonoValidado(boolean telefonoValidado) {
        this.telefonoValidado = telefonoValidado;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isRequiereValidacion() {
        return requiereValidacion;
    }

    public void setRequiereValidacion(boolean requiereValidacion) {
        this.requiereValidacion = requiereValidacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isYaValorado() {
        return yaValorado;
    }

    public void setYaValorado(boolean yaValorado) {
        this.yaValorado = yaValorado;
    }
}
