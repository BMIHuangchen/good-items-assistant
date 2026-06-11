package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.AppProperties;
import com.gooditems.config.RequestTraceFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    private final AppProperties properties;

    public HealthController(JdbcTemplate jdbc, AppProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    @GetMapping("/")
    public ApiResult<Map<String, Object>> index(HttpServletRequest request) {
        return ApiResult.ok(Map.of(
                "name", "好物展示小助手 API",
                "time", LocalDateTime.now(),
                "cosBaseUrl", properties.getCos().getBaseUrl()
        ), requestId(request));
    }

    @GetMapping("/api/diagnostics/ready")
    public ApiResult<Map<String, Object>> ready(HttpServletRequest request) {
        Integer dbOk = jdbc.queryForObject("select 1", Integer.class);
        return ApiResult.ok(Map.of(
                "database", dbOk != null && dbOk == 1 ? "ok" : "unknown",
                "cosBaseUrl", properties.getCos().getBaseUrl(),
                "publicBaseUrl", properties.getPublicBaseUrl(),
                "time", LocalDateTime.now()
        ), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
