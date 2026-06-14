package com.gooditems.dto;

public record BehaviorEventRequest(
        String eventType,
        String targetType,
        String targetId,
        String pagePath,
        String detail
) {
}
