package com.fixa.fixa_api.infrastructure.in.web.dto;

import lombok.Data;

@Data
public class ActualizarEmpresaRequest {
    private String nombre;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String email;
    private String bannerUrl;
    private String logoUrl;
    private Long categoriaId;

    // Configuración
    private boolean permiteReservasSinUsuario;
    private boolean requiereValidacionTelefono;
    private boolean requiereAprobacionTurno;
    private String mensajeValidacionPersonalizado;
    private boolean visibilidadPublica; // Esto controla "visible en listados", diferente a "activo" (ban/suspensión)
}
