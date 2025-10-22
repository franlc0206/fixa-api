package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class Empresa {
    private Long id;
    private Long usuarioAdminId;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String categoria;
    private boolean permiteReservasSinUsuario;
    private boolean requiereValidacionTelefono;
    private String mensajeValidacionPersonalizado;
    private boolean visibilidadPublica;
}
