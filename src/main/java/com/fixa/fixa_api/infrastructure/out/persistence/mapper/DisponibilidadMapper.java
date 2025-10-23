package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Disponibilidad;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.DisponibilidadEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpleadoEntity;

public class DisponibilidadMapper {
    public static Disponibilidad toDomain(DisponibilidadEntity e) {
        if (e == null) return null;
        Disponibilidad d = new Disponibilidad();
        d.setId(e.getId());
        d.setEmpleadoId(e.getEmpleado() != null ? e.getEmpleado().getId() : null);
        d.setDiaSemana(e.getDiaSemana());
        d.setHoraInicio(e.getHoraInicio());
        d.setHoraFin(e.getHoraFin());
        return d;
    }

    public static void copyToEntity(Disponibilidad d, DisponibilidadEntity e, EmpleadoEntity empleado) {
        e.setEmpleado(empleado);
        e.setDiaSemana(d.getDiaSemana());
        e.setHoraInicio(d.getHoraInicio());
        e.setHoraFin(d.getHoraFin());
    }
}
