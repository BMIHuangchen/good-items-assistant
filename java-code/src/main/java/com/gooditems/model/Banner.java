package com.gooditems.model;

import java.time.LocalDateTime;

public record Banner(
        Long id,
        String title,
        String imageUrl,
        String targetType,
        String targetValue,
        Integer sortOrder,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
