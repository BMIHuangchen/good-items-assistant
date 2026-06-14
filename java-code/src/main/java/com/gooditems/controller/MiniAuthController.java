package com.gooditems.controller;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import com.gooditems.dto.MiniLoginRequest;
import com.gooditems.dto.MiniLoginResponse;
import com.gooditems.model.MiniUser;
import com.gooditems.repository.UserRepository;
import com.gooditems.security.MiniTokenService;
import com.gooditems.service.WeChatMiniAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mini/auth")
public class MiniAuthController {
    private final WeChatMiniAuthService weChatAuth;
    private final UserRepository userRepository;
    private final MiniTokenService tokenService;

    public MiniAuthController(WeChatMiniAuthService weChatAuth, UserRepository userRepository, MiniTokenService tokenService) {
        this.weChatAuth = weChatAuth;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ApiResult<MiniLoginResponse> login(@Valid @RequestBody MiniLoginRequest body, HttpServletRequest request) {
        WeChatMiniAuthService.Session session = weChatAuth.code2Session(body.code());
        MiniUser user = userRepository.upsertLogin(session.openid(), session.unionid(), body.nickname(), body.avatarUrl(), requestId(request));
        String token = tokenService.issueToken(user.id(), user.openid());
        return ApiResult.ok(new MiniLoginResponse(token, user), requestId(request));
    }

    @GetMapping("/me")
    public ApiResult<MiniUser> me(@RequestHeader(value = "Authorization", required = false) String authorization,
                                  HttpServletRequest request) {
        Long userId = tokenService.requireUserId(authorization);
        return ApiResult.ok(userRepository.requireActive(userId), requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        return String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID));
    }
}
