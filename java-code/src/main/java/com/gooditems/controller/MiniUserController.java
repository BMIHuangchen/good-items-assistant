package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.BehaviorEventRequest;
import com.gooditems.dto.MiniUsageResponse;
import com.gooditems.model.GoodItem;
import com.gooditems.repository.UserRepository;
import com.gooditems.security.MiniTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mini/me")
public class MiniUserController {
    private final UserRepository userRepository;
    private final MiniTokenService tokenService;

    public MiniUserController(UserRepository userRepository, MiniTokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @GetMapping("/favorites")
    public ApiResult<List<GoodItem>> favorites(@RequestHeader(value = "Authorization", required = false) String authorization,
                                               HttpServletRequest request) {
        Long userId = requireUser(authorization);
        return ApiResult.ok(userRepository.favorites(userId), requestId(request));
    }

    @PostMapping("/favorites/{itemId}")
    public ApiResult<Void> addFavorite(@PathVariable Long itemId,
                                       @RequestHeader(value = "Authorization", required = false) String authorization,
                                       HttpServletRequest request) {
        Long userId = requireUser(authorization);
        userRepository.addFavorite(userId, itemId);
        return ApiResult.ok(requestId(request));
    }

    @DeleteMapping("/favorites/{itemId}")
    public ApiResult<Void> removeFavorite(@PathVariable Long itemId,
                                          @RequestHeader(value = "Authorization", required = false) String authorization,
                                          HttpServletRequest request) {
        Long userId = requireUser(authorization);
        userRepository.removeFavorite(userId, itemId);
        return ApiResult.ok(requestId(request));
    }

    @GetMapping("/ai-usage")
    public ApiResult<MiniUsageResponse> aiUsage(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                HttpServletRequest request) {
        Long userId = requireUser(authorization);
        return ApiResult.ok(userRepository.miniUsage(userId), requestId(request));
    }

    @PostMapping("/events")
    public ApiResult<Void> recordEvent(@RequestBody BehaviorEventRequest body,
                                       @RequestHeader(value = "Authorization", required = false) String authorization,
                                       HttpServletRequest request) {
        Long userId = requireUser(authorization);
        userRepository.recordEvent(userId, body, requestId(request));
        return ApiResult.ok(requestId(request));
    }

    private Long requireUser(String authorization) {
        Long userId = tokenService.requireUserId(authorization);
        userRepository.requireActive(userId);
        return userId;
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
