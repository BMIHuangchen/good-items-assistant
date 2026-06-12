package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.AiImageAnalysisResponse;
import com.gooditems.dto.AiMiniSettingsResponse;
import com.gooditems.service.AiImageAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mini/ai")
public class MiniAiController {
    private final AiImageAnalysisService service;

    public MiniAiController(AiImageAnalysisService service) {
        this.service = service;
    }

    @GetMapping("/settings")
    public ApiResult<AiMiniSettingsResponse> settings(HttpServletRequest request) {
        return ApiResult.ok(service.miniSettings(), requestId(request));
    }

    @PostMapping("/analyze-image")
    public ApiResult<AiImageAnalysisResponse> analyzeImage(@RequestParam String providerCode,
                                                           @RequestParam("file") MultipartFile file,
                                                           HttpServletRequest request) {
        return ApiResult.ok(service.analyze(providerCode, file, requestId(request)), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
