package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.common.PageResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.BannerRequest;
import com.gooditems.dto.CategoryRequest;
import com.gooditems.dto.GoodItemRequest;
import com.gooditems.dto.MiniProgramConfigRequest;
import com.gooditems.model.Banner;
import com.gooditems.model.Category;
import com.gooditems.model.DashboardStats;
import com.gooditems.model.GoodItem;
import com.gooditems.model.MiniProgramConfig;
import com.gooditems.repository.ContentRepository;
import com.gooditems.security.AdminTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {
    private final ContentRepository repository;
    private final AdminTokenService tokenService;

    public AdminContentController(ContentRepository repository, AdminTokenService tokenService) {
        this.repository = repository;
        this.tokenService = tokenService;
    }

    @GetMapping("/dashboard")
    public ApiResult<DashboardStats> dashboard(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.dashboardStats(), requestId(request));
    }

    @GetMapping("/mini-config")
    public ApiResult<MiniProgramConfig> miniConfig(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                  HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.miniProgramConfig(), requestId(request));
    }

    @PutMapping("/mini-config")
    public ApiResult<MiniProgramConfig> updateMiniConfig(@Valid @RequestBody MiniProgramConfigRequest body,
                                                        @RequestHeader(value = "Authorization", required = false) String authorization,
                                                        HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.updateMiniProgramConfig(body), requestId(request));
    }

    @GetMapping("/items")
    public ApiResult<PageResult<GoodItem>> items(@RequestParam(required = false) String status,
                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestHeader(value = "Authorization", required = false) String authorization,
                                                 HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.adminItems(status, Math.max(pageNum, 1), Math.min(pageSize, 50)), requestId(request));
    }

    @GetMapping("/items/{id}")
    public ApiResult<GoodItem> item(@PathVariable Long id,
                                    @RequestHeader(value = "Authorization", required = false) String authorization,
                                    HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.findItem(id), requestId(request));
    }

    @PostMapping("/items")
    public ApiResult<GoodItem> createItem(@Valid @RequestBody GoodItemRequest body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.createItem(body), requestId(request));
    }

    @PutMapping("/items/{id}")
    public ApiResult<GoodItem> updateItem(@PathVariable Long id,
                                          @Valid @RequestBody GoodItemRequest body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.updateItem(id, body), requestId(request));
    }

    @GetMapping("/categories")
    public ApiResult<List<Category>> categories(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.adminCategories(), requestId(request));
    }

    @PostMapping("/categories")
    public ApiResult<Category> createCategory(@Valid @RequestBody CategoryRequest body,
                                              @RequestHeader(value = "Authorization", required = false) String authorization,
                                              HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.createCategory(body), requestId(request));
    }

    @PutMapping("/categories/{id}")
    public ApiResult<Category> updateCategory(@PathVariable Long id,
                                              @Valid @RequestBody CategoryRequest body,
                                              @RequestHeader(value = "Authorization", required = false) String authorization,
                                              HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.updateCategory(id, body), requestId(request));
    }

    @GetMapping("/banners")
    public ApiResult<List<Banner>> banners(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.adminBanners(), requestId(request));
    }

    @PostMapping("/banners")
    public ApiResult<Banner> createBanner(@Valid @RequestBody BannerRequest body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.createBanner(body), requestId(request));
    }

    @PutMapping("/banners/{id}")
    public ApiResult<Banner> updateBanner(@PathVariable Long id,
                                          @Valid @RequestBody BannerRequest body,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          HttpServletRequest request) {
        tokenService.requireUsername(authorization);
        return ApiResult.ok(repository.updateBanner(id, body), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
