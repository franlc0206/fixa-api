package com.fixa.fixa_api.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VerificacionTelefono {
    private Long id;
    private String telefono;
    private String codigo;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaExpiracion;
    private boolean validado;
    private String canal; // sms, whatsapp
    private Long turnoId; // opcional
}
