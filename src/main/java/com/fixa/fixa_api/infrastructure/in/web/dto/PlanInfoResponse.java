package com.fixa.fixa_api.infrastructure.in.web.dto;

import lombok.Data;

@Data
public class PlanInfoResponse {
    private String planNombre;
    private LimitesInfo limites;

    @Data
    public static class LimitesInfo {
        private DetalleLimite empleados;
        private DetalleLimite servicios;
        private DetalleLimite turnosMensuales;
    }

    @Data
    public static class DetalleLimite {
        private long usado;
        private long maximo;

        public DetalleLimite(long usado, long maximo) {
            this.usado = usado;
            this.maximo = maximo;
        }
    }
}
