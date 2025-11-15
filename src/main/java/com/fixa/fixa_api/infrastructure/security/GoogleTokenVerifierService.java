package com.fixa.fixa_api.infrastructure.security;

import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GoogleTokenVerifierService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String clientId;

    public GoogleTokenVerifierService(@Value("${GOOGLE_CLIENT_ID:}") String clientId) {
        this.clientId = clientId;
    }

    public GoogleUserInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "idToken requerido");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID no configurado");
        }

        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, (Class<Map<String, Object>>)(Class<?>)Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
            }
            Map<String, Object> body = response.getBody();
            Object audObj = body.get("aud");
            String aud = audObj != null ? audObj.toString() : null;
            if (aud == null || !aud.equals(clientId)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
            }

            Object emailObj = body.get("email");
            String email = emailObj != null ? emailObj.toString() : null;
            if (email == null || email.isBlank()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
            }

            String givenName = body.get("given_name") != null ? body.get("given_name").toString() : null;
            String familyName = body.get("family_name") != null ? body.get("family_name").toString() : null;
            String fullName = body.get("name") != null ? body.get("name").toString() : null;

            return new GoogleUserInfo(email, givenName, familyName, fullName);
        } catch (RestClientException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
        }
    }

    public static class GoogleUserInfo {
        private final String email;
        private final String givenName;
        private final String familyName;
        private final String fullName;

        public GoogleUserInfo(String email, String givenName, String familyName, String fullName) {
            this.email = email;
            this.givenName = givenName;
            this.familyName = familyName;
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getGivenName() {
            return givenName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public String getFullName() {
            return fullName;
        }
    }
}
