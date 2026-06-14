package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.ComputeTierRequest;
import com.gooditems.dto.UserTierRequest;
import com.gooditems.model.ComputeTier;
import com.gooditems.model.MiniUser;
import com.gooditems.model.UserAiUsage;
import com.gooditems.repository.UserRepository;
import com.gooditems.security.AdminTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;
    private final AdminTokenService tokenService;

    public AdminUserController(UserRepository userRepository, AdminTokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @GetMapping
    public ApiResult<List<MiniUser>> users(@RequestParam(defaultValue = "100") int pageSize,
                                           @RequestHeader(value = "Authorization", required = false) String authorization,
                                           HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(userRepository.users(Math.min(Math.max(pageSize, 1), 200)), requestId(request));
    }

    @GetMapping("/ai-usage")
    public ApiResult<List<UserAiUsage>> aiUsage(@RequestParam(defaultValue = "100") int pageSize,
                                                @RequestHeader(value = "Authorization", required = false) String authorization,
                                                HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(userRepository.userAiUsage(Math.min(Math.max(pageSize, 1), 200)), requestId(request));
    }

    @GetMapping("/compute-tiers")
    public ApiResult<List<ComputeTier>> computeTiers(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                     HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(userRepository.computeTiers(), requestId(request));
    }

    @PutMapping("/compute-tiers/{tierCode}")
    public ApiResult<ComputeTier> updateComputeTier(@PathVariable String tierCode,
                                                    @RequestBody ComputeTierRequest body,
                                                    @RequestHeader(value = "Authorization", required = false) String authorization,
                                                    HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(userRepository.updateComputeTier(tierCode, body), requestId(request));
    }

    @PutMapping("/{id}/tier")
    public ApiResult<MiniUser> updateUserTier(@PathVariable Long id,
                                              @RequestBody UserTierRequest body,
                                              @RequestHeader(value = "Authorization", required = false) String authorization,
                                              HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(userRepository.updateUserTier(id, body), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
