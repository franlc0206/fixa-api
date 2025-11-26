package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class Categoria {
    private Long id;
    private String tipo; // empresa | servicio
    private String nombre;
    private String descripcion;
    private String icono;
    private String fotoDefault;
    private boolean activo;
}
