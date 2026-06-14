package com.gooditems.model;

import java.time.LocalDateTime;

public record MediaAsset(
        Long id,
        String source,
        String originalFilename,
        String mimeType,
        Long fileSize,
        String sha256,
        String objectKey,
        String publicUrl,
        LocalDateTime createdAt
) {
}
