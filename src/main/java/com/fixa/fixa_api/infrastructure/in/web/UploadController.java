package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.infrastructure.in.web.dto.UploadB2UrlRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.UploadB2UrlResponse;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.out.storage.BackblazeB2Client;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final BackblazeB2Client backblazeB2Client;

    public UploadController(BackblazeB2Client backblazeB2Client) {
        this.backblazeB2Client = backblazeB2Client;
    }

    @PostMapping("/b2-url")
    public ResponseEntity<UploadB2UrlResponse> getUploadUrl(@Valid @RequestBody UploadB2UrlRequest req) {
        String fileName = buildFileName(req);
        BackblazeB2Client.B2UploadAuthorization auth = backblazeB2Client.getUploadAuthorization();
        String publicUrl = auth.getPublicBaseUrl() + "/" + fileName;
        UploadB2UrlResponse resp = new UploadB2UrlResponse(
                auth.getUploadUrl(),
                auth.getAuthorizationToken(),
                fileName,
                publicUrl
        );
        return ResponseEntity.ok(resp);
    }

    private String buildFileName(UploadB2UrlRequest req) {
        String tipo = req.getTipo() != null ? req.getTipo().trim().toUpperCase() : "";
        String ext = normalizeExtension(req.getFileExtension());
        String randomId = UUID.randomUUID().toString().replace("-", "");

        switch (tipo) {
            case "EMPRESA_BANNER" -> {
                if (req.getEmpresaId() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "empresaId requerido para tipo EMPRESA_BANNER");
                }
                return "empresas/" + req.getEmpresaId() + "/banner-" + randomId + "." + ext;
            }
            case "EMPLEADO_FOTO" -> {
                if (req.getEmpleadoId() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "empleadoId requerido para tipo EMPLEADO_FOTO");
                }
                return "empleados/" + req.getEmpleadoId() + "-" + randomId + "." + ext;
            }
            case "SERVICIO_FOTO" -> {
                if (req.getServicioId() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "servicioId requerido para tipo SERVICIO_FOTO");
                }
                return "servicios/" + req.getServicioId() + "-" + randomId + "." + ext;
            }
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "tipo invalido para upload B2");
        }
    }

    private String normalizeExtension(String ext) {
        if (ext == null || ext.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "fileExtension requerido");
        }
        String normalized = ext.trim().toLowerCase();
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
