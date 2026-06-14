package com.gooditems.dto;

import com.gooditems.model.MiniUser;

public record MiniLoginResponse(
        String token,
        MiniUser user
) {
}
