package com.gooditems.dto;

import java.math.BigDecimal;
import java.util.List;

public record MiniUsageResponse(
        Summary today,
        Summary month,
        List<ModelUsage> models,
        List<RecentAiTask> recentTasks
) {
    public record Summary(
            Long callCount,
            Long successCount,
            Long totalTokens,
            BigDecimal estimatedCost
    ) {
    }

    public record ModelUsage(
            String providerCode,
            String modelName,
            Long callCount,
            Long totalTokens,
            BigDecimal estimatedCost
    ) {
    }

    public record RecentAiTask(
            Long taskId,
            String providerCode,
            String status,
            String itemTitle,
            String mediaUrl,
            String createdAt
    ) {
    }
}
