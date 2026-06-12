package com.gooditems.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AiConfirmTaskRequest(
        Long categoryId,
        Boolean createNewCategory,
        String newCategoryName,
        String newCategorySlug,
        String newCategoryDescription,
        @NotBlank String itemTitle,
        String summary,
        String experience,
        List<String> tags,
        Boolean publish
) {
}
