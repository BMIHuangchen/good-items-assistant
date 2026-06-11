package com.gooditems.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record MiniProgramConfigRequest(
        @NotBlank String heroEyebrow,
        @NotBlank String heroTitle,
        @NotBlank String heroSubtitle,
        @NotBlank String featuredTitle,
        @NotBlank String searchPlaceholder,
        List<String> hotWords,
        @NotBlank String meTitle,
        @NotBlank String meDescription
) {}
