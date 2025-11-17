package com.fixa.fixa_api.infrastructure.out.storage;

import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class BackblazeB2Client {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String keyId;
    private final String applicationKey;
    private final String bucketId;
    private final String bucketName;

    public BackblazeB2Client(
            @Value("${B2_KEY_ID:}") String keyId,
            @Value("${B2_APPLICATION_KEY:}") String applicationKey,
            @Value("${B2_BUCKET_ID:}") String bucketId,
            @Value("${B2_BUCKET_NAME:}") String bucketName
    ) {
        this.keyId = keyId;
        this.applicationKey = applicationKey;
        this.bucketId = bucketId;
        this.bucketName = bucketName;
    }

    public B2UploadAuthorization getUploadAuthorization() {
        StringBuilder missing = new StringBuilder();
        if (isBlank(keyId)) {
            missing.append("B2_KEY_ID ");
        }
        if (isBlank(applicationKey)) {
            missing.append("B2_APPLICATION_KEY ");
        }
        if (isBlank(bucketId)) {
            missing.append("B2_BUCKET_ID ");
        }
        if (isBlank(bucketName)) {
            missing.append("B2_BUCKET_NAME ");
        }
        if (missing.length() > 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Backblaze B2 no esta configurado correctamente. Faltan: " + missing.toString().trim());
        }

        try {
            String credentials = keyId + ":" + applicationKey;
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.set("Authorization", basicAuth);
            HttpEntity<Void> authEntity = new HttpEntity<>(null, authHeaders);

            ResponseEntity<Map<String, Object>> authResponse = restTemplate.exchange(
                    "https://api.backblazeb2.com/b2api/v2/b2_authorize_account",
                    HttpMethod.GET,
                    authEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (!authResponse.getStatusCode().is2xxSuccessful() || authResponse.getBody() == null) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo autorizar cuenta B2");
            }

            Map<String, Object> authBody = authResponse.getBody();
            String accountAuthToken = toStringOrNull(authBody.get("authorizationToken"));
            String apiUrl = toStringOrNull(authBody.get("apiUrl"));
            String downloadUrl = toStringOrNull(authBody.get("downloadUrl"));

            if (accountAuthToken == null || apiUrl == null || downloadUrl == null) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Respuesta invalida de Backblaze B2");
            }

            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.set("Authorization", accountAuthToken);
            Map<String, String> body = Map.of("bucketId", bucketId);
            HttpEntity<Map<String, String>> uploadEntity = new HttpEntity<>(body, uploadHeaders);

            ResponseEntity<Map<String, Object>> uploadResponse = restTemplate.exchange(
                    apiUrl + "/b2api/v2/b2_get_upload_url",
                    HttpMethod.POST,
                    uploadEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (!uploadResponse.getStatusCode().is2xxSuccessful() || uploadResponse.getBody() == null) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo obtener uploadUrl de B2");
            }

            Map<String, Object> uploadBody = uploadResponse.getBody();
            String uploadUrl = toStringOrNull(uploadBody.get("uploadUrl"));
            String uploadAuthToken = toStringOrNull(uploadBody.get("authorizationToken"));

            if (uploadUrl == null || uploadAuthToken == null) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Respuesta invalida de B2 al obtener uploadUrl");
            }

            String publicBaseUrl = downloadUrl + "/file/" + bucketName;

            return new B2UploadAuthorization(uploadUrl, uploadAuthToken, publicBaseUrl);
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de comunicacion con Backblaze B2");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String toStringOrNull(Object value) {
        return value != null ? value.toString() : null;
    }

    public static class B2UploadAuthorization {
        private final String uploadUrl;
        private final String authorizationToken;
        private final String publicBaseUrl;

        public B2UploadAuthorization(String uploadUrl, String authorizationToken, String publicBaseUrl) {
            this.uploadUrl = uploadUrl;
            this.authorizationToken = authorizationToken;
            this.publicBaseUrl = publicBaseUrl;
        }

        public String getUploadUrl() {
            return uploadUrl;
        }

        public String getAuthorizationToken() {
            return authorizationToken;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }
    }
}
