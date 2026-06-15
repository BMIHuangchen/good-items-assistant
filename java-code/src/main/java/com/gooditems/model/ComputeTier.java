package com.gooditems.model;

import java.time.LocalDateTime;

public record ComputeTier(
        String tierCode,
        String tierName,
        Long dailyTokenLimit,
        Long monthlyTokenLimit,
        Integer dailyCallLimit,
        Boolean enabled,
        Integer sortOrder,
        LocalDateTime updatedAt
) {
}
