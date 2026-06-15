package com.gooditems.dto;

public record UserTierRequest(
        String tierCode,
        Long customDailyTokenLimit,
        Long customMonthlyTokenLimit,
        Integer customDailyCallLimit
) {
}
