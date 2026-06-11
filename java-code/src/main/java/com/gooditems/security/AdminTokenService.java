package com.gooditems.security;

import com.gooditems.config.AppProperties;
import com.gooditems.exception.ApiException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class AdminTokenService {
    private final AppProperties properties;

    public AdminTokenService(AppProperties properties) {
        this.properties = properties;
    }

    public String issueToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("roles", List.of("OWNER", "CONTENT_EDITOR", "REVIEWER", "OPS"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 60 * 8)))
                .signWith(key())
                .compact();
    }

    public String requireUsername(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(401, "请先登录管理后台");
        }
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authorization.substring(7))
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            throw new ApiException(401, "登录状态无效或已过期");
        }
    }

    private SecretKey key() {
        byte[] bytes = properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new ApiException(500, "JWT_SECRET 至少需要 32 个字符");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
