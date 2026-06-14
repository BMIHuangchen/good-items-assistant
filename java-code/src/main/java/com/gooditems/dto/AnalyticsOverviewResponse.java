package com.gooditems.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsOverviewResponse(
        Long totalUsers,
        Long todayLogins,
        Long todayActiveUsers,
        Long totalBehaviorEvents,
        Long totalAiCalls,
        Long totalAiTokens,
        BigDecimal totalAiEstimatedCost,
        List<TrendPoint> loginTrend,
        List<TrendPoint> aiTrend,
        List<TrendPoint> aiTokenTrend,
        List<NameValue> eventRanking,
        List<NameValue> modelUsage,
        List<ModelComputeUsage> modelComputeUsage,
        List<UserComputeRank> userComputeRanking,
        List<NameValue> hotItems
) {
    public record TrendPoint(String date, Long value) {
    }

    public record NameValue(String name, Long value) {
    }

    public record ModelComputeUsage(
            String providerCode,
            String modelName,
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

    public record UserComputeRank(
            Long userId,
            String openidMask,
            String nickname,
            String tierCode,
            String tierName,
            Long callCount,
            Long totalTokens,
            BigDecimal estimatedCost
    ) {
    }
}
