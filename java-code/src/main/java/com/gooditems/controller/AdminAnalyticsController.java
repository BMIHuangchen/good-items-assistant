package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.AnalyticsOverviewResponse;
import com.gooditems.repository.UserRepository;
import com.gooditems.security.AdminTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {
    private final UserRepository userRepository;
    private final AdminTokenService tokenService;

    public AdminAnalyticsController(UserRepository userRepository, AdminTokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @GetMapping("/overview")
    public ApiResult<AnalyticsOverviewResponse> overview(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                         HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(userRepository.analyticsOverview(), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
