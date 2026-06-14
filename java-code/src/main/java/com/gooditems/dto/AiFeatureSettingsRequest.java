package com.gooditems.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record AiFeatureSettingsRequest(
        Boolean aiEnabled,
        Boolean autoIngestEnabled,
        Boolean autoPublishEnabled,
        Boolean lowConfidenceReviewEnabled,
        @DecimalMin("0.0000") @DecimalMax("1.0000") BigDecimal confidenceThreshold,
        @Min(1) @Max(1000) Integer dailyCallLimit,
        @Min(1) @Max(10) Integer maxImageSizeMb
) {
}
