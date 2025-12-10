package com.fixa.fixa_api.domain.model.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardMetrics {
    private long totalTurnosMes;
    private BigDecimal ingresosEstimadosMes;
    private Map<String, Long> turnosPorEstado;
    private List<EmpleadoMetrica> topEmpleados;
    private List<ServicioMetrica> topServicios;
    private Map<String, Long> turnosPorMesUltimoAno;

    @Data
    @Builder
    public static class EmpleadoMetrica {
        private Long empleadoId;
        private String nombre;
        private String apellido;
        private long cantidadTurnos;
    }

    @Data
    @Builder
    public static class ServicioMetrica {
        private Long servicioId;
        private String nombre;
        private long cantidadTurnos;
        private BigDecimal ingresosGenerados;
    }
}
