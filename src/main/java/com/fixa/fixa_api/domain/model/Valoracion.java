package com.fixa.fixa_api.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Valoracion {
    private Long id;
    private Long empresaId;
    private Long usuarioId;
    private Long turnoId;
    private Integer puntuacion; // 0-5 estrellas
    private String resena;
    private LocalDateTime fechaCreacion;
    private boolean activo;
}
