package com.fixa.fixa_api.infrastructure.in.web.dto;

public class UploadB2UrlResponse {

    private String uploadUrl;
    private String authorizationToken;
    private String fileName;
    private String publicUrl;

    public UploadB2UrlResponse(String uploadUrl, String authorizationToken, String fileName, String publicUrl) {
        this.uploadUrl = uploadUrl;
        this.authorizationToken = authorizationToken;
        this.fileName = fileName;
        this.publicUrl = publicUrl;
    }

    public String getUploadUrl() { return uploadUrl; }
    public String getAuthorizationToken() { return authorizationToken; }
    public String getFileName() { return fileName; }
    public String getPublicUrl() { return publicUrl; }
}
