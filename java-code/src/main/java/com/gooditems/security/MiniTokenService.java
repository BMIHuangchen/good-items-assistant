package com.gooditems.security;

import com.gooditems.config.AppProperties;
import com.gooditems.exception.ApiException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class MiniTokenService {
    private final AppProperties properties;

    public MiniTokenService(AppProperties properties) {
        this.properties = properties;
    }

    public String issueToken(Long userId, String openid) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("openid", openid)
                .claim("scope", "mini")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Math.max(1, properties.getMiniProgram().getTokenExpireDays()), ChronoUnit.DAYS)))
                .signWith(key())
                .compact();
    }

    public Long requireUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(401, "请先登录后再使用");
        }
        try {
            String subject = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authorization.substring(7))
                    .getPayload()
                    .getSubject();
            return Long.parseLong(subject);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(401, "登录状态已过期，请重新登录");
        }
    }

    private SecretKey key() {
        String secret = properties.getMiniProgram().getJwtSecret();
        if (secret == null || secret.length() < 32) {
            secret = properties.getJwtSecret();
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
