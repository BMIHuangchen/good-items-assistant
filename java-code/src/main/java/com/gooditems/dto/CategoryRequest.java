package com.gooditems.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        String slug,
        String description,
        String coverImage,
        Integer sortOrder,
        Boolean enabled
) {
}
