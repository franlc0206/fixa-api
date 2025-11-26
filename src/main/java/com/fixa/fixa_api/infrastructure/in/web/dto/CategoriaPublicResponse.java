package com.fixa.fixa_api.infrastructure.in.web.dto;

import com.fixa.fixa_api.domain.model.Categoria;
import com.fixa.fixa_api.infrastructure.util.SlugUtils;

public class CategoriaPublicResponse {
    private Long id;
    private String nombre;
    private String slug;
    private String tipo;
    private String icono;
    private String fotoDefault;

    public static CategoriaPublicResponse fromDomain(Categoria categoria) {
        CategoriaPublicResponse dto = new CategoriaPublicResponse();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setTipo(categoria.getTipo());
        String slug = SlugUtils.toSlug(categoria.getNombre());
        if (slug == null || slug.isBlank()) {
            slug = categoria.getId() != null ? "categoria-" + categoria.getId() : null;
        }
        dto.setSlug(slug);
        dto.setIcono(categoria.getIcono());
        dto.setFotoDefault(categoria.getFotoDefault());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getFotoDefault() {
        return fotoDefault;
    }

    public void setFotoDefault(String fotoDefault) {
        this.fotoDefault = fotoDefault;
    }
}
