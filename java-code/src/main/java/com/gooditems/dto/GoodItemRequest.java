package com.gooditems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GoodItemRequest(
        @NotNull Long categoryId,
        @NotBlank String title,
        String summary,
        String experience,
        List<String> tags,
        @NotBlank String coverImage,
        List<String> gallery,
        String status,
        Integer sortOrder
) {
}
