package com.fixa.fixa_api.domain.repository;

import java.util.Optional;

public interface ConfigReglaQueryPort {
    Optional<Integer> getInt(Long empresaId, String clave);
}
