package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.infrastructure.in.web.dto.UploadB2UrlRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.UploadB2UrlResponse;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.out.storage.BackblazeB2Client;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

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

    @PostMapping("/b2")
    public ResponseEntity<UploadB2UrlResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "empresaId", required = false) Long empresaId,
            @RequestParam(value = "empleadoId", required = false) Long empleadoId,
            @RequestParam(value = "servicioId", required = false) Long servicioId
    ) {
        logger.info("[UploadController] Iniciando upload B2: tipo={}, empresaId={}, empleadoId={}, servicioId={}, nombre={}, size={} bytes",
                tipo, empresaId, empleadoId, servicioId,
                file != null ? file.getOriginalFilename() : null,
                file != null ? file.getSize() : null);

        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "file requerido");
        }

        String extension = "jpg";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.') + 1).trim();
            if (!ext.isBlank()) {
                extension = ext.toLowerCase();
            }
        }

        UploadB2UrlRequest req = new UploadB2UrlRequest();
        req.setTipo(tipo);
        req.setEmpresaId(empresaId);
        req.setEmpleadoId(empleadoId);
        req.setServicioId(servicioId);
        req.setFileExtension(extension);

        String fileName = buildFileName(req);
        logger.info("[UploadController] Nombre final del archivo para B2: {} (extension={})", fileName, extension);
        BackblazeB2Client.B2UploadAuthorization auth = backblazeB2Client.getUploadAuthorization();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", auth.getAuthorizationToken());
            headers.set("X-Bz-File-Name", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            headers.set("X-Bz-Content-Sha1", "do_not_verify");
            String contentType = file.getContentType() != null ? file.getContentType() : "b2/x-auto";
            headers.set("Content-Type", contentType);

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> uploadResponse = restTemplate.exchange(
                    auth.getUploadUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
                logger.error("[UploadController] Subida a B2 fallo. Status={}, body={}",
                        uploadResponse.getStatusCode(), uploadResponse.getBody());
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo subir archivo a B2");
            }
        } catch (IOException | RestClientException ex) {
            logger.error("[UploadController] Error de comunicacion con Backblaze B2", ex);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de comunicacion con Backblaze B2");
        }

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
