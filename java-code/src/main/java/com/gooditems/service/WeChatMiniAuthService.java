package com.gooditems.service;

import com.gooditems.config.AppProperties;
import com.gooditems.exception.ApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class WeChatMiniAuthService {
    private final AppProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WeChatMiniAuthService(AppProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public Session code2Session(String code) {
        String appId = properties.getMiniProgram().getAppId();
        String appSecret = properties.getMiniProgram().getAppSecret();
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            String normalized = code == null || code.isBlank() ? "local" : code.replaceAll("[^a-zA-Z0-9_-]", "");
            return new Session("dev_openid_" + normalized, null);
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code"
                .formatted(encode(appId), encode(appSecret), encode(code));
        String response;
        try {
            response = restClient.get().uri(url).retrieve().body(String.class);
        } catch (RestClientException e) {
            throw new ApiException(502, "微信登录接口请求失败");
        }
        Map<?, ?> body = parseResponse(response);
        if (body == null) {
            throw new ApiException(502, "微信登录接口无响应");
        }
        Object errCode = body.get("errcode");
        if (errCode instanceof Number number && number.intValue() != 0) {
            Object message = body.get("errmsg");
            throw new ApiException(502, "微信登录失败：" + (message == null ? "code2session error" : message));
        }
        Object openid = body.get("openid");
        if (openid == null || String.valueOf(openid).isBlank()) {
            throw new ApiException(502, "微信登录未返回 openid");
        }
        return new Session(String.valueOf(openid), body.get("unionid") == null ? null : String.valueOf(body.get("unionid")));
    }

    private Map<?, ?> parseResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(502, "微信登录接口响应格式异常");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record Session(String openid, String unionid) {
    }
}
