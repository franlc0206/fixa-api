package com.fixa.fixa_api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicioEtapa {
    public enum TipoEtapa {
        TRABAJO,
        ESPERA
    }

    private TipoEtapa tipo;
    private int duracionMinutos;
}
