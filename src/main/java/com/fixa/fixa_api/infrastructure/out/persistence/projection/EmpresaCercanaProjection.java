package com.fixa.fixa_api.infrastructure.out.persistence.projection;

public interface EmpresaCercanaProjection {
    Long getId();

    String getNombre();

    String getSlug();

    String getDescripcion();

    String getDireccion();

    String getTelefono();

    String getEmail();

    String getBannerUrl();

    String getLogoUrl();

    Boolean getVisibilidadPublica();

    Boolean getActivo();

    Double getLatitud();

    Double getLongitud();

    Double getDistancia();

    // Helper accessors for other columns if needed, matching EmpresaEntity
    // Assuming simple mapping for now.
    // For relationships (Categoria, Plan, Admin), projection might need nesting
    // or just IDs if we want to keep it simple.
    // For this use case, we mostly need basic info and IDs.

    // We can use Spring Data Property Expressions (e.g. getCategoriaId())
    // if the query selects "e.categoria.id as categoriaId".
    // But my native query did "SELECT *".
    // Native query "SELECT *" with Interface Projection works if column names match
    // getters.
    // "fk_categoria" -> might need alias in query "fk_categoria as categoriaId" or
    // similar.
    // Or just map what I need.

    Long getCategoriaId();
    // Spring Data usually expects camelCase matches to column aliases or property
    // names.
    // I will use explicit aliases in the SQL to match camelCase here.
}
