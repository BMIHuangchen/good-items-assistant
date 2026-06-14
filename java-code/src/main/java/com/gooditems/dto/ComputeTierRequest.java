package com.gooditems.dto;

public record ComputeTierRequest(
        String tierName,
        Long dailyTokenLimit,
        Long monthlyTokenLimit,
        Integer dailyCallLimit,
        Boolean enabled,
        Integer sortOrder
) {
}
