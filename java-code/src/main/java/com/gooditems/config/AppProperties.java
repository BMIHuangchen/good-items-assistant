package com.gooditems.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String publicBaseUrl;
    private String adminUsername;
    private String adminPassword;
    private String jwtSecret;
    private final Cos cos = new Cos();
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
    public Diagnostics getDiagnostics() { return diagnostics; }

    public static class Cos {
        private String baseUrl;
        private String bucket;
        private String region;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
    }

    public static class Diagnostics {
        private long slowRequestMs = 1200;

        public long getSlowRequestMs() { return slowRequestMs; }
        public void setSlowRequestMs(long slowRequestMs) { this.slowRequestMs = slowRequestMs; }
    }
}
