package com.runshot.user.controller;

import com.runshot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(
            @RequestBody UserCreateRequest request
    ) {
        return userService.createUser(request);
    }
}