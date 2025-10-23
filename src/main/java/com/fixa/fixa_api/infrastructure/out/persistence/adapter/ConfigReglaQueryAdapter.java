package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.repository.ConfigReglaQueryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.ConfigReglaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.ConfigReglaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConfigReglaQueryAdapter implements ConfigReglaQueryPort {

    private final ConfigReglaJpaRepository repo;

    public ConfigReglaQueryAdapter(ConfigReglaJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Integer> getInt(Long empresaId, String clave) {
        // Implementación básica: buscar todas y filtrar en memoria (optimizable con query específica)
        return repo.findAll().stream()
                .filter(r -> r.getEmpresa() != null)
                .filter(r -> r.getEmpresa().getId().equals(empresaId))
                .filter(r -> r.isActivo())
                .filter(r -> clave.equalsIgnoreCase(r.getClave()))
                .map(ConfigReglaEntity::getValor)
                .map(v -> {
                    try { return Integer.parseInt(v); } catch (Exception ex) { return null; }
                })
                .filter(v -> v != null)
                .findFirst();
    }
}
