package com.gooditems.dto;

import java.math.BigDecimal;
import java.util.List;

public record AiImageAnalysisResponse(
        Long taskId,
        String status,
        String ingestMode,
        String mediaUrl,
        String providerCode,
        String modelName,
        String itemTitle,
        String summary,
        String experience,
        List<String> tags,
        String decision,
        Long matchedCategoryId,
        String matchedCategoryName,
        String newCategoryName,
        BigDecimal confidence,
        String reason,
        Long createdItemId,
        Long createdCategoryId,
        String reviewReason
) {
}
