package com.gooditems.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AiImageAnalysisTask(
        Long id,
        String requestId,
        Long mediaAssetId,
        String mediaUrl,
        String providerCode,
        String modelName,
        String status,
        String ingestMode,
        String itemTitle,
        String summary,
        String experience,
        List<String> tags,
        String decision,
        Long matchedCategoryId,
        String matchedCategoryName,
        String newCategoryName,
        String newCategorySlug,
        String newCategoryDescription,
        BigDecimal confidence,
        String reason,
        String reviewReason,
        Long createdCategoryId,
        Long createdItemId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
