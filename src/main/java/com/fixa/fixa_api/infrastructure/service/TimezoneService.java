package com.fixa.fixa_api.infrastructure.service;

import com.fixa.fixa_api.domain.model.Empresa;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
public class TimezoneService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    public ZoneId getZoneIdForEmpresa(Empresa empresa) {
        if (empresa == null || empresa.getLongitud() == null) {
            return DEFAULT_ZONE;
        }

        // Lógica de estimación basada en Longitud
        // La Tierra tiene 360 grados y 24 horas. 360 / 24 = 15 grados por hora.
        // Offset aproximado = Longitud / 15.
        // Ejemplo: Buenos Aires (-58.38) / 15 = -3.89 -> Round a -4.
        // Nota: Argentina políticamente usa UTC-3, pero geográficamente es UTC-4.
        // Usar UTC-4 significa cerrar los turnos 1 hora "más tarde" de lo real (buffer
        // natural), lo cual es seguro.
        // Cerrarlos antes de tiempo ("UTC-3" cuando en realidad están en UTC-4) sería
        // riesgoso.

        try {
            int offsetHours = (int) Math.round(empresa.getLongitud() / 15.0);
            return ZoneId.ofOffset("UTC", ZoneOffset.ofHours(offsetHours));
        } catch (Exception e) {
            // Fallback en caso de cualquier error matemático o de rango
            return DEFAULT_ZONE;
        }
    }
}
