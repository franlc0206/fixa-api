package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    private final EmpresaRepositoryPort empresaPort;

    public EmpresaService(EmpresaRepositoryPort empresaPort) {
        this.empresaPort = empresaPort;
    }

    public List<Empresa> listar(Boolean visibles) {
        return Boolean.TRUE.equals(visibles) ? empresaPort.findVisibles() : empresaPort.findAll();
    }

    public Optional<Empresa> obtener(Long id) {
        return empresaPort.findById(id);
    }

    public Empresa guardar(Empresa empresa) {
        return empresaPort.save(empresa);
    }

    public boolean activar(Long id, boolean activo) {
        Optional<Empresa> opt = empresaPort.findById(id);
        if (opt.isEmpty()) return false;
        Empresa e = opt.get();
        e.setActivo(activo);
        empresaPort.save(e);
        return true;
    }
}
