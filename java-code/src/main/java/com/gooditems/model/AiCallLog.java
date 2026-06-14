package com.gooditems.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiCallLog(
        Long id,
        String requestId,
        Long userId,
        String openidMask,
        String providerCode,
        String modelName,
        String scenario,
        String status,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        BigDecimal estimatedCost,
        Integer durationMs,
        String errorMessage,
        Long taskId,
        LocalDateTime createdAt
) {
}
