package com.gooditems.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String username,
        String displayName,
        List<String> roles
) {
}
