package com.fixa.fixa_api.infrastructure.in.scheduler;

import com.fixa.fixa_api.application.usecase.CompletarTurnoUseCase;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.infrastructure.service.TimezoneService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TurnoCompletionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TurnoCompletionScheduler.class);

    private final TurnoRepositoryPort turnoRepo;
    private final CompletarTurnoUseCase completarTurnoUseCase;
    private final EmpresaRepositoryPort empresaRepo;
    private final TimezoneService timezoneService;

    public TurnoCompletionScheduler(TurnoRepositoryPort turnoRepo,
            CompletarTurnoUseCase completarTurnoUseCase,
            EmpresaRepositoryPort empresaRepo,
            TimezoneService timezoneService) {
        this.turnoRepo = turnoRepo;
        this.completarTurnoUseCase = completarTurnoUseCase;
        this.empresaRepo = empresaRepo;
        this.timezoneService = timezoneService;
    }

    /**
     * Ejecuta cada 15 minutos.
     * Busca turnos CONFIRMADOS que ya pasaron su fecha de fin y los marca como
     * COMPLETADO.
     * Utiliza la zona horaria de la empresa para asegurar precisión.
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void completarTurnosPasados() {
        logger.info("Iniciando job de completitud de turnos...");
        try {
            // 1. Buscar candidatos con un margen de seguridad (ej: pasaron hace 1 hora
            // según servidor)
            // Esto es para filtrar el grueso en DB. Luego refinamos en memoria.
            // Usamos now() del servidor.
            LocalDateTime serverNow = LocalDateTime.now();
            List<Turno> candidatos = turnoRepo.findByEstadoAndFechaHoraFinBefore("CONFIRMADO", serverNow);

            if (candidatos.isEmpty()) {
                logger.info("No hay turnos pendientes de completar.");
                return;
            }

            logger.info("Encontrados {} candidatos preliminares para completar.", candidatos.size());

            // 2. Agrupar por Empresa para optimizar queries de configuración y zona horaria
            Map<Long, List<Turno>> porEmpresa = candidatos.stream()
                    .collect(Collectors.groupingBy(Turno::getEmpresaId));

            int completados = 0;

            for (Map.Entry<Long, List<Turno>> entry : porEmpresa.entrySet()) {
                Long empresaId = entry.getKey();
                List<Turno> turnosEmpresa = entry.getValue();

                try {
                    // Obtener empresa y zona horaria
                    Empresa empresa = empresaRepo.findById(empresaId).orElse(null);
                    ZoneId zoneId = timezoneService.getZoneIdForEmpresa(empresa);

                    // Hora actual en la zona de la empresa
                    ZonedDateTime nowInZone = ZonedDateTime.now(zoneId);
                    LocalDateTime localNow = nowInZone.toLocalDateTime();

                    // Buffer de seguridad: Esperar 15 minutos extra después del fin del turno
                    // para dar tiempo a operaciones manuales de último momento.
                    LocalDateTime safeThreshold = localNow.minusMinutes(15);

                    for (Turno turno : turnosEmpresa) {
                        // Validar si efectivamente pasó la fecha fin + buffer en su zona local
                        if (turno.getFechaHoraFin().isBefore(safeThreshold)) {
                            try {
                                completarTurnoUseCase.completar(turno.getId());
                                completados++;
                            } catch (Exception e) {
                                logger.error("Error al completar turno {}: {}", turno.getId(), e.getMessage());
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Error procesando lote de empresa {}: {}", empresaId, ex.getMessage());
                }
            }
            logger.info("Job finalizado. Turnos completados: {}", completados);

        } catch (Exception e) {
            logger.error("Error fatal en TurnoCompletionScheduler", e);
        }
    }
}
