package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class Empleado {
    private Long id;
    private Long empresaId;
    private Long usuarioId; // opcional
    private String nombre;
    private String apellido;
    private String rol; // peluquero, colorista, etc.
    private String fotoUrl;
    private boolean trabajaPublicamente;
    private boolean activo;
}
