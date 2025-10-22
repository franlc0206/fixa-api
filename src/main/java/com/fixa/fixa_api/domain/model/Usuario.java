package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class Usuario {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String rol; // superadmin, empresa, empleado, cliente
    private boolean activo;
}
