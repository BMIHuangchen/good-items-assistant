package com.gooditems.model;

public record DashboardStats(
        long publishedItems,
        long draftItems,
        long categories,
        long banners,
        long totalViews,
        long totalFavorites
) {
}
