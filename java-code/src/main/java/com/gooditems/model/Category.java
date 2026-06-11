package com.gooditems.model;

import java.time.LocalDateTime;

public record Category(
        Long id,
        String name,
        String slug,
        String description,
        String coverImage,
        Integer sortOrder,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
