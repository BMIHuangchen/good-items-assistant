package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.AiConfirmTaskRequest;
import com.gooditems.dto.AiImageAnalysisResponse;
import com.gooditems.dto.AiMiniSettingsResponse;
import com.gooditems.repository.UserRepository;
import com.gooditems.security.MiniTokenService;
import com.gooditems.service.AiImageAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mini/ai")
public class MiniAiController {
    private final AiImageAnalysisService service;
    private final MiniTokenService tokenService;
    private final UserRepository userRepository;

    public MiniAiController(AiImageAnalysisService service, MiniTokenService tokenService, UserRepository userRepository) {
        this.service = service;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @GetMapping("/settings")
    public ApiResult<AiMiniSettingsResponse> settings(HttpServletRequest request) {
        return ApiResult.ok(service.miniSettings(), requestId(request));
    }

    @PostMapping("/analyze-image")
    public ApiResult<AiImageAnalysisResponse> analyzeImage(@RequestParam String providerCode,
                                                           @RequestParam("file") MultipartFile file,
                                                           @RequestHeader(value = "Authorization", required = false) String authorization,
                                                           HttpServletRequest request) {
        Long userId = requireUser(authorization);
        return ApiResult.ok(service.analyze(userId, providerCode, file, requestId(request)), requestId(request));
    }

    @PostMapping("/image-tasks/{id}/confirm")
    public ApiResult<AiImageAnalysisResponse> confirm(@PathVariable Long id,
                                                      @Valid @RequestBody AiConfirmTaskRequest body,
                                                      @RequestHeader(value = "Authorization", required = false) String authorization,
                                                      HttpServletRequest request) {
        Long userId = requireUser(authorization);
        return ApiResult.ok(service.response(service.confirm(id, body, userId)), requestId(request));
    }

    private Long requireUser(String authorization) {
        Long userId = tokenService.requireUserId(authorization);
        userRepository.requireActive(userId);
        return userId;
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
