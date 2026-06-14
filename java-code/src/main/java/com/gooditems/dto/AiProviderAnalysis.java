package com.gooditems.dto;

import java.math.BigDecimal;
import java.util.List;

public record AiProviderAnalysis(
        String itemTitle,
        String summary,
        String experience,
        List<String> tags,
        String decision,
        Long categoryId,
        String newCategoryName,
        String newCategorySlug,
        String newCategoryDescription,
        BigDecimal confidence,
        String reason,
        String rawJson,
        int promptTokens,
        int completionTokens,
        int totalTokens
) {
}
