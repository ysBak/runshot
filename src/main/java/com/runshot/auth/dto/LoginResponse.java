
package com.runshot.auth.dto;

import com.runshot.user.entity.User;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String accessToken,
        String tokenType,
        long expiresIn
) {

    public static LoginResponse from(
            User user,
            String accessToken,
            long expiresIn
    ) {
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                accessToken,
                "Bearer",
                expiresIn
        );
    }
}
