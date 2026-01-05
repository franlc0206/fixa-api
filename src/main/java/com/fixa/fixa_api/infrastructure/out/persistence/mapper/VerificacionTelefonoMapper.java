package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.VerificacionTelefono;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.TurnoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.VerificacionTelefonoEntity;

/**
 * Mapper entre VerificacionTelefono (dominio) y VerificacionTelefonoEntity
 * (JPA).
 * Siguiendo arquitectura hexagonal, este mapper pertenece a la capa de
 * infraestructura
 * y convierte entre el modelo de dominio y la entidad de persistencia.
 */
public class VerificacionTelefonoMapper {

    public static VerificacionTelefono toDomain(VerificacionTelefonoEntity entity) {
        if (entity == null)
            return null;

        VerificacionTelefono domain = new VerificacionTelefono();
        domain.setId(entity.getId());
        domain.setTelefono(entity.getTelefono());
        domain.setEmail(entity.getEmail());
        domain.setCodigo(entity.getCodigo());
        domain.setFechaEnvio(entity.getFechaEnvio());
        domain.setFechaExpiracion(entity.getFechaExpiracion());
        domain.setValidado(entity.isValidado());
        domain.setCanal(entity.getCanal());
        domain.setTurnoId(entity.getTurno() != null ? entity.getTurno().getId() : null);

        return domain;
    }

    public static void copyToEntity(VerificacionTelefono domain, VerificacionTelefonoEntity entity, TurnoEntity turno) {
        entity.setTelefono(domain.getTelefono());
        entity.setEmail(domain.getEmail());
        entity.setCodigo(domain.getCodigo());
        entity.setFechaEnvio(domain.getFechaEnvio());
        entity.setFechaExpiracion(domain.getFechaExpiracion());
        entity.setValidado(domain.isValidado());
        entity.setCanal(domain.getCanal());
        entity.setTurno(turno);
    }
}
