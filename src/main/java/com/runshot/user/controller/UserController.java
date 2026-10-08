package com.runshot.user.controller;

import com.runshot.user.service.UserService;
import com.runshot.user.dto.UserCreateRequest;
import com.runshot.user.dto.UserResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse createUser(
            @RequestBody UserCreateRequest request
    ) {
        return userService.createUser(request);
    }
}