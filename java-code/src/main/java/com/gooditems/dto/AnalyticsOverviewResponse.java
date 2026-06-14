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
        List<NameValue> eventRanking,
        List<NameValue> modelUsage,
        List<NameValue> hotItems
) {
    public record TrendPoint(String date, Long value) {
    }

    public record NameValue(String name, Long value) {
    }
}
