package com.fixa.fixa_api.infrastructure.util;

public final class SlugUtils {

    private SlugUtils() {
        // utility class
    }

    /**
     * Genera un slug a partir de un nombre.
     * Convierte a minúsculas, reemplaza espacios por guiones,
     * elimina caracteres especiales y acentos.
     */
    public static String toSlug(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        return nombre
                .toLowerCase()
                .trim()
                // Reemplazar acentos
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                // Reemplazar espacios y caracteres especiales por guiones
                .replaceAll("[\\s]+", "-")
                .replaceAll("[^a-z0-9-]", "")
                // Eliminar guiones duplicados
                .replaceAll("-+", "-")
                // Eliminar guiones al inicio y final
                .replaceAll("^-|-$", "");
    }
}
