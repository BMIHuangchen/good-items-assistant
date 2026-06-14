package com.gooditems.model;

import java.math.BigDecimal;

public record UserAiUsage(
        Long userId,
        String openidMask,
        String nickname,
        Long callCount,
        Long successCount,
        Long failedCount,
        Long promptTokens,
        Long completionTokens,
        Long totalTokens,
        BigDecimal estimatedCost,
        Long avgDurationMs
) {
}
