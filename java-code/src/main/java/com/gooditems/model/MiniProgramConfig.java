package com.gooditems.model;

import java.time.LocalDateTime;
import java.util.List;

public record MiniProgramConfig(
        Long id,
        String heroEyebrow,
        String heroTitle,
        String heroSubtitle,
        String featuredTitle,
        String searchPlaceholder,
        List<String> hotWords,
        String meTitle,
        String meDescription,
        LocalDateTime updatedAt
) {}
