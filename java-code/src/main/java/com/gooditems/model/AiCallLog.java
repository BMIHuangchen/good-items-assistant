package com.gooditems.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiCallLog(
        Long id,
        String requestId,
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
