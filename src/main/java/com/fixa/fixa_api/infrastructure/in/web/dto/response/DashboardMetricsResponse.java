package com.fixa.fixa_api.infrastructure.in.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardMetricsResponse {
    private long totalTurnosMes;
    private BigDecimal ingresosEstimadosMes;
    private Map<String, Long> turnosPorEstado;
    private List<EmpleadoMetricaDTO> topEmpleados;
    private List<ServicioMetricaDTO> topServicios;
    private Map<String, Long> turnosPorMesUltimoAno;

    @Data
    @Builder
    public static class EmpleadoMetricaDTO {
        private Long empleadoId;
        private String nombre;
        private String apellido;
        private long cantidadTurnos;
    }

    @Data
    @Builder
    public static class ServicioMetricaDTO {
        private Long servicioId;
        private String nombre;
        private long cantidadTurnos;
        private BigDecimal ingresosGenerados;
    }
}
