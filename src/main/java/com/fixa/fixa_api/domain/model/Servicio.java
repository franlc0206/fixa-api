package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class Servicio {
    private Long id;
    private Long empresaId;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private boolean requiereEspacioLibre;
    private java.math.BigDecimal costo;
    private boolean requiereSena;
    private boolean activo;
}
