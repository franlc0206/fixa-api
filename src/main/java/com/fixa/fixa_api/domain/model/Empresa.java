package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class Empresa {
    private Long id;
    private Long usuarioAdminId;
    private String nombre;
    private String slug;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String email;
    private String bannerUrl;
    private String logoUrl;
    private Long categoriaId;
    private boolean permiteReservasSinUsuario;
    private boolean requiereValidacionTelefono;
    private boolean requiereAprobacionTurno;
    private String mensajeValidacionPersonalizado;
    private boolean visibilidadPublica;
    private Double latitud;
    private Double longitud;
    private Double distancia; // Distancia calculada en KM, transient
    private boolean activo;
    private Long planActualId;
}
