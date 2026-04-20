package com.app.fisiolab_system.dto;

import org.jspecify.annotations.Nullable;

public record LoginResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresIn) {

    public @Nullable Object type() {
        return tokenType;
    }
}
