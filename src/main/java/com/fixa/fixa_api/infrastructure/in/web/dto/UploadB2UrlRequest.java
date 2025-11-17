package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public class UploadB2UrlRequest {

    @NotBlank
    private String tipo; // EMPRESA_BANNER, EMPLEADO_FOTO, SERVICIO_FOTO

    private Long empresaId;
    private Long empleadoId;
    private Long servicioId;

    @NotBlank
    private String fileExtension; // jpg, png, etc.

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    public Long getServicioId() { return servicioId; }
    public void setServicioId(Long servicioId) { this.servicioId = servicioId; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }
}
