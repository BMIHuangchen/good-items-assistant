package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.AiConfirmTaskRequest;
import com.gooditems.dto.AiFeatureSettingsRequest;
import com.gooditems.dto.AiImageAnalysisResponse;
import com.gooditems.dto.AiModelConfigRequest;
import com.gooditems.model.AiCallLog;
import com.gooditems.model.AiFeatureSettings;
import com.gooditems.model.AiImageAnalysisTask;
import com.gooditems.model.AiModelConfig;
import com.gooditems.repository.AiRepository;
import com.gooditems.security.AdminTokenService;
import com.gooditems.service.AiImageAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiController {
    private final AiRepository aiRepository;
    private final AiImageAnalysisService service;
    private final AdminTokenService tokenService;

    public AdminAiController(AiRepository aiRepository, AiImageAnalysisService service, AdminTokenService tokenService) {
        this.aiRepository = aiRepository;
        this.service = service;
        this.tokenService = tokenService;
    }

    @GetMapping("/settings")
    public ApiResult<AiFeatureSettings> settings(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                 HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(aiRepository.settings(), requestId(request));
    }

    @PutMapping("/settings")
    public ApiResult<AiFeatureSettings> updateSettings(@Valid @RequestBody AiFeatureSettingsRequest body,
                                                       @RequestHeader(value = "Authorization", required = false) String authorization,
                                                       HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(aiRepository.updateSettings(body), requestId(request));
    }

    @GetMapping("/models")
    public ApiResult<List<AiModelConfig>> models(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                 HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(aiRepository.models(false), requestId(request));
    }

    @PutMapping("/models/{id}")
    public ApiResult<AiModelConfig> updateModel(@PathVariable Long id,
                                                @Valid @RequestBody AiModelConfigRequest body,
                                                @RequestHeader(value = "Authorization", required = false) String authorization,
                                                HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(aiRepository.updateModel(id, body), requestId(request));
    }

    @GetMapping("/image-tasks")
    public ApiResult<List<AiImageAnalysisTask>> tasks(@RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "50") int pageSize,
                                                      @RequestHeader(value = "Authorization", required = false) String authorization,
                                                      HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(aiRepository.tasks(status, Math.min(Math.max(pageSize, 1), 100)), requestId(request));
    }

    @PostMapping("/image-tasks/{id}/confirm")
    public ApiResult<AiImageAnalysisResponse> confirm(@PathVariable Long id,
                                                      @Valid @RequestBody AiConfirmTaskRequest body,
                                                      @RequestHeader(value = "Authorization", required = false) String authorization,
                                                      HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(service.response(service.confirm(id, body)), requestId(request));
    }

    @PostMapping("/image-tasks/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id,
                                  @RequestBody(required = false) Map<String, String> body,
                                  @RequestHeader(value = "Authorization", required = false) String authorization,
                                  HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        service.reject(id, body == null ? null : body.get("reason"));
        return ApiResult.ok(requestId(request));
    }

    @GetMapping("/call-logs")
    public ApiResult<List<AiCallLog>> callLogs(@RequestParam(defaultValue = "80") int pageSize,
                                               @RequestHeader(value = "Authorization", required = false) String authorization,
                                               HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(aiRepository.callLogs(Math.min(Math.max(pageSize, 1), 200)), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
