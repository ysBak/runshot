
package com.runshot.auth.dto;

import com.runshot.user.entity.User;

public record LoginResponse(
        Long userId,
        String email,
        String nickname
) {

    public static LoginResponse from(User user) {
        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}
