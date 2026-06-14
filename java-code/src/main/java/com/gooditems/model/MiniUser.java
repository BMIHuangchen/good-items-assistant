package com.gooditems.model;

import java.time.LocalDateTime;

public record MiniUser(
        Long id,
        String openid,
        String unionid,
        String nickname,
        String avatarUrl,
        String status,
        String tierCode,
        Long customDailyTokenLimit,
        Long customMonthlyTokenLimit,
        Integer customDailyCallLimit,
        Integer loginCount,
        LocalDateTime firstLoginAt,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
