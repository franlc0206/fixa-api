package com.fixa.fixa_api.domain.model;

import lombok.Data;

@Data
public class UsuarioEmpresa {
    private Long id;
    private Long usuarioId;
    private Long empresaId;
    private String rolEmpresa; // OWNER | MANAGER | STAFF
    private boolean activo;
}
