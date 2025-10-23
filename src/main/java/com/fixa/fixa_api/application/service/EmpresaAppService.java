package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.CategoriaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpresaJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaAppService {

    private final EmpresaJpaRepository empresaRepo;
    private final CategoriaJpaRepository categoriaRepo;

    public EmpresaAppService(EmpresaJpaRepository empresaRepo, CategoriaJpaRepository categoriaRepo) {
        this.empresaRepo = empresaRepo;
        this.categoriaRepo = categoriaRepo;
    }

    public List<EmpresaEntity> listar(Boolean visibles) {
        if (Boolean.TRUE.equals(visibles)) {
            return empresaRepo.findByVisibilidadPublicaTrue();
        }
        return empresaRepo.findAll();
    }

    public Optional<EmpresaEntity> obtener(Long id) {
        return empresaRepo.findById(id);
    }

    public EmpresaEntity crearOActualizar(EmpresaEntity empresa, Long categoriaId) {
        if (categoriaId != null) {
            CategoriaEntity cat = categoriaRepo.findById(categoriaId).orElse(null);
            empresa.setCategoria(cat);
        } else {
            empresa.setCategoria(null);
        }
        return empresaRepo.save(empresa);
    }

    public boolean activar(Long id, boolean activo) {
        return empresaRepo.findById(id).map(e -> {
            e.setActivo(activo);
            empresaRepo.save(e);
            return true;
        }).orElse(false);
    }
}
