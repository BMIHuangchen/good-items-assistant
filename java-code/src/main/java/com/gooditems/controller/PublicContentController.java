package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.common.PageResult;
import com.gooditems.config.AppProperties;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.CosConfigResponse;
import com.gooditems.model.Banner;
import com.gooditems.model.Category;
import com.gooditems.model.GoodItem;
import com.gooditems.model.MiniProgramConfig;
import com.gooditems.repository.ContentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/mini")
public class PublicContentController {
    private final ContentRepository repository;
    private final AppProperties properties;

    public PublicContentController(ContentRepository repository, AppProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @GetMapping("/banners")
    public ApiResult<List<Banner>> banners(HttpServletRequest request) {
        return ApiResult.ok(repository.publicBanners(), requestId(request));
    }

    @GetMapping("/config")
    public ApiResult<MiniProgramConfig> config(HttpServletRequest request) {
        return ApiResult.ok(repository.miniProgramConfig(), requestId(request));
    }

    @GetMapping("/categories")
    public ApiResult<List<Category>> categories(HttpServletRequest request) {
        return ApiResult.ok(repository.publicCategories(), requestId(request));
    }

    @GetMapping("/items")
    public ApiResult<PageResult<GoodItem>> items(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        return ApiResult.ok(repository.publicItems(categoryId, keyword, Math.max(pageNum, 1), Math.min(pageSize, 30)), requestId(request));
    }

    @GetMapping("/items/{id}")
    public ApiResult<GoodItem> item(@PathVariable Long id, HttpServletRequest request) {
        return ApiResult.ok(repository.publicItem(id), requestId(request));
    }

    @GetMapping("/cos")
    public ApiResult<CosConfigResponse> cos(HttpServletRequest request) {
        return ApiResult.ok(new CosConfigResponse(
                properties.getCos().getBaseUrl(),
                properties.getCos().getBucket(),
                properties.getCos().getRegion(),
                "小程序图片统一使用腾讯 COS/CDN 域名，避免依赖外部随机图片源。"
        ), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
