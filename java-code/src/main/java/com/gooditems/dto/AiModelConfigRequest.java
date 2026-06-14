package com.gooditems.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AiModelConfigRequest(
        @NotBlank String displayName,
        @NotBlank String modelName,
        @NotBlank String baseUrl,
        @NotBlank String apiKeyEnv,
        Boolean enabled,
        BigDecimal promptPricePer1k,
        BigDecimal completionPricePer1k,
        Integer sortOrder
) {
}
