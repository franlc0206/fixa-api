package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.dashboard.DashboardMetrics;

import java.time.LocalDate;

public interface DashboardRepositoryPort {
    DashboardMetrics obtenerMetricas(Long empresaId, LocalDate inicio, LocalDate fin);
}
