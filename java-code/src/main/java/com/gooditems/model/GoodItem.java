package com.gooditems.model;

import java.time.LocalDateTime;
import java.util.List;

public record GoodItem(
        Long id,
        Long categoryId,
        String categoryName,
        String title,
        String summary,
        String experience,
        List<String> tags,
        String coverImage,
        List<String> gallery,
        String status,
        Integer sortOrder,
        Integer viewCount,
        Integer favoriteCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
