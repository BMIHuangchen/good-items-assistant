package com.gooditems.model;

public record UserComputeQuota(
        Long userId,
        String tierCode,
        String tierName,
        Long dailyTokenLimit,
        Long monthlyTokenLimit,
        Integer dailyCallLimit,
        Long todayTokens,
        Long monthTokens,
        Integer todayCalls
) {
}
