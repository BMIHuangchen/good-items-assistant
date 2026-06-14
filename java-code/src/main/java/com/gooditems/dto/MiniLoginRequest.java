package com.gooditems.dto;

import jakarta.validation.constraints.NotBlank;

public record MiniLoginRequest(
        @NotBlank String code,
        String nickname,
        String avatarUrl
) {
}
