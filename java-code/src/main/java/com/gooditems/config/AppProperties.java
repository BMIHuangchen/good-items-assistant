package com.gooditems.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String publicBaseUrl;
    private String adminUsername;
    private String adminPassword;
    private String jwtSecret;
    private final Cos cos = new Cos();
    private final Ai ai = new Ai();
    private final MiniProgram miniProgram = new MiniProgram();
    private final Diagnostics diagnostics = new Diagnostics();

    public String getPublicBaseUrl() { return publicBaseUrl; }
    public void setPublicBaseUrl(String publicBaseUrl) { this.publicBaseUrl = publicBaseUrl; }
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
    public Cos getCos() { return cos; }
    public Ai getAi() { return ai; }
    public MiniProgram getMiniProgram() { return miniProgram; }
    public Diagnostics getDiagnostics() { return diagnostics; }

    public static class Cos {
        private String baseUrl;
        private String bucket;
        private String region;
        private String secretId;
        private String secretKey;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getSecretId() { return secretId; }
        public void setSecretId(String secretId) { this.secretId = secretId; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    public static class Ai {
        private String kimiApiKey;
        private String doubaoApiKey;

        public String getKimiApiKey() { return kimiApiKey; }
        public void setKimiApiKey(String kimiApiKey) { this.kimiApiKey = kimiApiKey; }
        public String getDoubaoApiKey() { return doubaoApiKey; }
        public void setDoubaoApiKey(String doubaoApiKey) { this.doubaoApiKey = doubaoApiKey; }
    }

    public static class MiniProgram {
        private String appId;
        private String appSecret;
        private String jwtSecret;
        private int tokenExpireDays = 30;

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
        public String getAppSecret() { return appSecret; }
        public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
        public int getTokenExpireDays() { return tokenExpireDays; }
        public void setTokenExpireDays(int tokenExpireDays) { this.tokenExpireDays = tokenExpireDays; }
    }

    public static class Diagnostics {
        private long slowRequestMs = 1200;

        public long getSlowRequestMs() { return slowRequestMs; }
        public void setSlowRequestMs(long slowRequestMs) { this.slowRequestMs = slowRequestMs; }
    }
}
