package com.gooditems.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiModelConfig(
        Long id,
        String providerCode,
        String displayName,
        String modelName,
        String baseUrl,
        String apiKeyEnv,
        Boolean enabled,
        BigDecimal promptPricePer1k,
        BigDecimal completionPricePer1k,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
