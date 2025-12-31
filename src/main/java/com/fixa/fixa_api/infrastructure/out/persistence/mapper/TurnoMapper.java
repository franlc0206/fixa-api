package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.TurnoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.ServicioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpleadoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.TurnoEstado;

public class TurnoMapper {

    public static Turno toDomain(TurnoEntity e) {
        if (e == null)
            return null;
        Turno d = new Turno();
        d.setId(e.getId());
        d.setServicioId(e.getServicio() != null ? e.getServicio().getId() : null);
        d.setEmpleadoId(e.getEmpleado() != null ? e.getEmpleado().getId() : null);
        d.setEmpresaId(e.getEmpresa() != null ? e.getEmpresa().getId() : null);
        d.setClienteId(e.getCliente() != null ? e.getCliente().getId() : null);
        d.setClienteNombre(e.getClienteNombre());
        d.setClienteApellido(e.getClienteApellido());
        d.setClienteTelefono(e.getClienteTelefono());
        d.setClienteDni(e.getClienteDni());
        d.setClienteEmail(e.getClienteEmail());
        d.setTelefonoValidado(e.isTelefonoValidado());
        d.setFechaHoraInicio(e.getFechaHoraInicio());
        d.setFechaHoraFin(e.getFechaHoraFin());
        d.setEstado(e.getEstado() != null ? e.getEstado().name() : null);
        d.setRequiereValidacion(e.isRequiereValidacion());
        d.setObservaciones(e.getObservaciones());

        // Mapeo campos enriquecidos
        if (e.getServicio() != null) {
            d.setServicioNombre(e.getServicio().getNombre());
        }
        if (e.getEmpresa() != null) {
            d.setEmpresaNombre(e.getEmpresa().getNombre());
            d.setEmpresaLogoUrl(e.getEmpresa().getLogoUrl());
        }
        if (e.getEmpleado() != null) {
            String nombreCompleto = (e.getEmpleado().getNombre() != null ? e.getEmpleado().getNombre() : "") + " " +
                    (e.getEmpleado().getApellido() != null ? e.getEmpleado().getApellido() : "");
            d.setEmpleadoNombre(nombreCompleto.trim());
        }

        if (e.getServicio() != null && e.getServicio().getCosto() != null) {
            d.setPrecio(e.getServicio().getCosto());
        } else {
            d.setPrecio(java.math.BigDecimal.ZERO);
        }
        return d;
    }

    public static void copyToEntity(
            Turno d,
            TurnoEntity e,
            ServicioEntity servicio,
            EmpleadoEntity empleado,
            EmpresaEntity empresa,
            UsuarioEntity cliente) {
        e.setServicio(servicio);
        e.setEmpleado(empleado);
        e.setEmpresa(empresa);
        e.setCliente(cliente);
        e.setClienteNombre(d.getClienteNombre());
        e.setClienteApellido(d.getClienteApellido());
        e.setClienteTelefono(d.getClienteTelefono());
        e.setClienteDni(d.getClienteDni());
        e.setClienteEmail(d.getClienteEmail());
        e.setTelefonoValidado(d.isTelefonoValidado());
        e.setFechaHoraInicio(d.getFechaHoraInicio());
        e.setFechaHoraFin(d.getFechaHoraFin());
        if (d.getEstado() != null) {
            e.setEstado(TurnoEstado.valueOf(d.getEstado().toUpperCase()));
        }
        e.setRequiereValidacion(d.isRequiereValidacion());
        e.setObservaciones(d.getObservaciones());

    }
}
