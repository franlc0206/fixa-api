package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.dashboard.DashboardMetrics;
import com.fixa.fixa_api.domain.repository.DashboardRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DashboardService {

    private final DashboardRepositoryPort dashboardRepositoryPort;

    public DashboardService(DashboardRepositoryPort dashboardRepositoryPort) {
        this.dashboardRepositoryPort = dashboardRepositoryPort;
    }

    @Transactional(readOnly = true)
    public DashboardMetrics obtenerMetricas(Long empresaId, LocalDate inicio, LocalDate fin) {
        // Validaciones de negocio si fueran necesarias
        if (inicio == null) {
            inicio = LocalDate.now().withDayOfMonth(1);
        }
        if (fin == null) {
            fin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        return dashboardRepositoryPort.obtenerMetricas(empresaId, inicio, fin);
    }
}
