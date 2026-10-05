package com.runshot.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 4, max = 100)
        String password,

        @NotBlank
        @Size(max = 30)
        String nickname
) {
}