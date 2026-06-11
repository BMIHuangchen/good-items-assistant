package com.gooditems.dto;

public record CosConfigResponse(
        String baseUrl,
        String bucket,
        String region,
        String note
) {
}
