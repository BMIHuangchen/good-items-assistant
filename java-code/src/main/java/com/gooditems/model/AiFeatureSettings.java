package com.gooditems.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AiFeatureSettings(
        Long id,
        Boolean aiEnabled,
        Boolean autoIngestEnabled,
        Boolean autoPublishEnabled,
        Boolean lowConfidenceReviewEnabled,
        BigDecimal confidenceThreshold,
        Integer dailyCallLimit,
        Integer maxImageSizeMb,
        LocalDateTime updatedAt
) {
}
