package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.infrastructure.in.web.dto.UploadB2UrlRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.UploadB2UrlResponse;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.out.storage.BackblazeB2Client;
import jakarta.validation.Valid;
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
                publicUrl);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/b2")
    public ResponseEntity<UploadB2UrlResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "empresaId", required = false) Long empresaId,
            @RequestParam(value = "empleadoId", required = false) Long empleadoId,
            @RequestParam(value = "servicioId", required = false) Long servicioId,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId) {
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
        req.setCategoriaId(categoriaId);
        req.setFileExtension(extension);

        String fileName = buildFileName(req);
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
                    String.class);

            if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo subir archivo a B2");
            }
        } catch (IOException | RestClientException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de comunicacion con Backblaze B2");
        }

        String publicUrl = auth.getPublicBaseUrl() + "/" + fileName;
        UploadB2UrlResponse resp = new UploadB2UrlResponse(
                auth.getUploadUrl(),
                auth.getAuthorizationToken(),
                fileName,
                publicUrl);
        return ResponseEntity.ok(resp);
    }

    private String buildFileName(UploadB2UrlRequest req) {
        String tipo = req.getTipo() != null ? req.getTipo().trim().toUpperCase() : "";
        String ext = normalizeExtension(req.getFileExtension());
        String randomId = UUID.randomUUID().toString().replace("-", "");

        switch (tipo) {
            case "EMPRESA_BANNER" -> {
                String prefix = req.getEmpresaId() != null ? "empresas/" + req.getEmpresaId() : "empresas/pending";
                return prefix + "/banner-" + randomId + "." + ext;
            }
            case "EMPLEADO_FOTO" -> {
                String prefix = req.getEmpleadoId() != null ? "empleados/" + req.getEmpleadoId() : "empleados/pending";
                return prefix + "-" + randomId + "." + ext;
            }
            case "SERVICIO_FOTO" -> {
                String prefix = req.getServicioId() != null ? "servicios/" + req.getServicioId() : "servicios/pending";
                return prefix + "-" + randomId + "." + ext;
            }
            case "EMPRESA_LOGO" -> {
                String prefix = req.getEmpresaId() != null ? "empresas/" + req.getEmpresaId() : "empresas/pending";
                return prefix + "/logo-" + randomId + "." + ext;
            }
            case "CATEGORIA_FOTO_DEFAULT" -> {
                if (req.getCategoriaId() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "categoriaId requerido para tipo CATEGORIA_FOTO_DEFAULT");
                }
                return "categorias/" + req.getCategoriaId() + "/default-" + randomId + "." + ext;
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
