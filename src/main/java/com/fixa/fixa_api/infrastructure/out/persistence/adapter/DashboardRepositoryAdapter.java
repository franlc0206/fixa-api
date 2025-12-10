package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.dashboard.DashboardMetrics;
import com.fixa.fixa_api.domain.repository.DashboardRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.TurnoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DashboardRepositoryAdapter implements DashboardRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DashboardMetrics obtenerMetricas(Long empresaId, LocalDate inicio, LocalDate fin) {
        return DashboardMetrics.builder()
                .totalTurnosMes(contarTurnosEnRango(empresaId, inicio, fin))
                .ingresosEstimadosMes(calcularIngresosEnRango(empresaId, inicio, fin))
                .turnosPorEstado(agruparTurnosPorEstado(empresaId, inicio, fin))
                .topEmpleados(obtenerTopEmpleados(empresaId, inicio, fin))
                .topServicios(obtenerTopServicios(empresaId, inicio, fin))
                .turnosPorMesUltimoAno(obtenerTurnosPorMesUltimoAno(empresaId))
                .build();
    }

    private long contarTurnosEnRango(Long empresaId, LocalDate inicio, LocalDate fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<TurnoEntity> root = query.from(TurnoEntity.class);

        query.select(cb.count(root));
        query.where(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.between(root.get("fechaHoraInicio"), inicio.atStartOfDay(), fin.atTime(23, 59, 59)));

        return entityManager.createQuery(query).getSingleResult();
    }

    private BigDecimal calcularIngresosEnRango(Long empresaId, LocalDate inicio, LocalDate fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<TurnoEntity> root = query.from(TurnoEntity.class);

        // Sumar costo del servicio. Asumimos que el costo está en el servicio.
        // TODO: Si el costo varía por turno, debería estar en TurnoEntity.
        // Por ahora usamos el del servicio asociado.
        Join<Object, Object> servicio = root.join("servicio");

        query.select(cb.sum(servicio.get("costo")));
        query.where(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.between(root.get("fechaHoraInicio"), inicio.atStartOfDay(), fin.atTime(23, 59, 59)),
                root.get("estado").in("CONFIRMADO", "COMPLETADO") // Solo contar confirmados/completados
        );

        BigDecimal result = entityManager.createQuery(query).getSingleResult();
        return result != null ? result : BigDecimal.ZERO;
    }

    private Map<String, Long> agruparTurnosPorEstado(Long empresaId, LocalDate inicio, LocalDate fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<TurnoEntity> root = query.from(TurnoEntity.class);

        query.multiselect(root.get("estado"), cb.count(root));
        query.where(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.between(root.get("fechaHoraInicio"), inicio.atStartOfDay(), fin.atTime(23, 59, 59)));
        query.groupBy(root.get("estado"));

        List<Tuple> results = entityManager.createQuery(query).getResultList();
        Map<String, Long> map = new HashMap<>();
        for (Tuple t : results) {
            map.put(t.get(0).toString(), (Long) t.get(1));
        }
        return map;
    }

    private List<DashboardMetrics.EmpleadoMetrica> obtenerTopEmpleados(Long empresaId, LocalDate inicio,
            LocalDate fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<TurnoEntity> root = query.from(TurnoEntity.class);
        Join<Object, Object> empleado = root.join("empleado");

        query.multiselect(
                empleado.get("id"),
                empleado.get("nombre"),
                empleado.get("apellido"),
                cb.count(root));
        query.where(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.between(root.get("fechaHoraInicio"), inicio.atStartOfDay(), fin.atTime(23, 59, 59)));
        query.groupBy(empleado.get("id"), empleado.get("nombre"), empleado.get("apellido"));
        query.orderBy(cb.desc(cb.count(root)));

        List<Tuple> results = entityManager.createQuery(query).setMaxResults(5).getResultList();

        return results.stream().map(t -> DashboardMetrics.EmpleadoMetrica.builder()
                .empleadoId((Long) t.get(0))
                .nombre((String) t.get(1))
                .apellido((String) t.get(2))
                .cantidadTurnos((Long) t.get(3))
                .build()).collect(Collectors.toList());
    }

    private List<DashboardMetrics.ServicioMetrica> obtenerTopServicios(Long empresaId, LocalDate inicio,
            LocalDate fin) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<TurnoEntity> root = query.from(TurnoEntity.class);
        Join<Object, Object> servicio = root.join("servicio");

        query.multiselect(
                servicio.get("id"),
                servicio.get("nombre"),
                cb.count(root),
                cb.sum(servicio.get("costo")));
        query.where(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.between(root.get("fechaHoraInicio"), inicio.atStartOfDay(), fin.atTime(23, 59, 59)));
        query.groupBy(servicio.get("id"), servicio.get("nombre"));
        query.orderBy(cb.desc(cb.count(root)));

        List<Tuple> results = entityManager.createQuery(query).setMaxResults(5).getResultList();

        return results.stream().map(t -> DashboardMetrics.ServicioMetrica.builder()
                .servicioId((Long) t.get(0))
                .nombre((String) t.get(1))
                .cantidadTurnos((Long) t.get(2))
                .ingresosGenerados(t.get(3) != null ? (BigDecimal) t.get(3) : BigDecimal.ZERO)
                .build()).collect(Collectors.toList());
    }

    private Map<String, Long> obtenerTurnosPorMesUltimoAno(Long empresaId) {
        // Esto es más complejo con Criteria estándar por la extracción de mes/año.
        // Usaremos una query nativa o JPQL simplificado si la DB lo soporta,
        // pero para portabilidad intentaremos hacerlo en memoria o con function.
        // Para simplificar y asegurar compatibilidad, traeremos los turnos del último
        // año y agruparemos en Java.
        // Ojo con performance si son muchos. Para MVP está bien.

        LocalDate haceUnAno = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        LocalDate fin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<TurnoEntity> root = query.from(TurnoEntity.class);

        query.multiselect(root.get("fechaHoraInicio"));
        query.where(
                cb.equal(root.get("empresa").get("id"), empresaId),
                cb.between(root.get("fechaHoraInicio"), haceUnAno.atStartOfDay(), fin.atTime(23, 59, 59)));

        List<Tuple> results = entityManager.createQuery(query).getResultList();

        Map<String, Long> map = new TreeMap<>(); // Ordenado
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // Inicializar últimos 12 meses en 0
        LocalDate current = haceUnAno;
        while (!current.isAfter(fin)) {
            map.put(current.format(formatter), 0L);
            current = current.plusMonths(1);
        }

        for (Tuple t : results) {
            java.time.LocalDateTime fecha = (java.time.LocalDateTime) t.get(0);
            String key = fecha.format(formatter);
            map.put(key, map.getOrDefault(key, 0L) + 1);
        }

        return map;
    }
}
