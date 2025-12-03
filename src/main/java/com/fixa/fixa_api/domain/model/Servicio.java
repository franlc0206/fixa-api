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
    private String patronBloques;
    private java.math.BigDecimal costo;
    private boolean requiereSena;
    private boolean activo;
    private Long categoriaId; // opcional
    private String fotoUrl;

    public java.util.List<ServicioEtapa> getEtapas() {
        if (patronBloques == null || patronBloques.isBlank()) {
            // Si no hay patrón, es un solo bloque de trabajo de la duración total
            return java.util.List.of(
                    new ServicioEtapa(ServicioEtapa.TipoEtapa.TRABAJO, duracionMinutos != null ? duracionMinutos : 0));
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return java.util.Arrays.asList(mapper.readValue(patronBloques, ServicioEtapa[].class));
        } catch (Exception e) {
            // Si falla el parseo, fallback a comportamiento default
            return java.util.List.of(
                    new ServicioEtapa(ServicioEtapa.TipoEtapa.TRABAJO, duracionMinutos != null ? duracionMinutos : 0));
        }
    }
}
