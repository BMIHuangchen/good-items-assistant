package com.gooditems.dto;

import jakarta.validation.constraints.NotBlank;

public record BannerRequest(
        @NotBlank String title,
        @NotBlank String imageUrl,
        String targetType,
        String targetValue,
        Integer sortOrder,
        Boolean enabled
) {
}
