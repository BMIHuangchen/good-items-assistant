package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.AppProperties;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.LoginRequest;
import com.gooditems.dto.LoginResponse;
import com.gooditems.exception.ApiException;
import com.gooditems.security.AdminTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    private final AppProperties properties;
    private final AdminTokenService tokenService;

    public AdminAuthController(AppProperties properties, AdminTokenService tokenService) {
        this.properties = properties;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        if (!properties.getAdminUsername().equals(body.username()) || !properties.getAdminPassword().equals(body.password())) {
            throw new ApiException(401, "账号或密码不正确");
        }
        return ApiResult.ok(new LoginResponse(
                tokenService.issueToken(body.username()),
                body.username(),
                "内容管理负责人",
                List.of("OWNER", "CONTENT_EDITOR", "REVIEWER", "OPS")
        ), requestId(request));
    }

    @GetMapping("/me")
    public ApiResult<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             HttpServletRequest request) {
        String username = tokenService.requireUsername(authorization);
        return ApiResult.ok(Map.of(
                "username", username,
                "displayName", "内容管理负责人",
                "roles", List.of("OWNER", "CONTENT_EDITOR", "REVIEWER", "OPS")
        ), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
