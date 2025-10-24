package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario_empresa", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_empresa", columnNames = {"usuario_id", "empresa_id"})
})
public class UsuarioEmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @Column(name = "rol_empresa", length = 30)
    private String rolEmpresa; // OWNER | MANAGER | STAFF

    @Column
    private boolean activo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsuarioEntity getUsuario() { return usuario; }
    public void setUsuario(UsuarioEntity usuario) { this.usuario = usuario; }
    public EmpresaEntity getEmpresa() { return empresa; }
    public void setEmpresa(EmpresaEntity empresa) { this.empresa = empresa; }
    public String getRolEmpresa() { return rolEmpresa; }
    public void setRolEmpresa(String rolEmpresa) { this.rolEmpresa = rolEmpresa; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
