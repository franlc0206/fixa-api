package com.fixa.fixa_api.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class RefreshToken {
    private Long id;
    private Long usuarioId;
    private String token;
    private Instant expiryDate;
}
