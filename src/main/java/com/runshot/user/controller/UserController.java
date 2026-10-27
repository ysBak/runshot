package com.runshot.user.controller;

import com.runshot.user.service.UserService;
import com.runshot.user.dto.UserCreateRequest;
import com.runshot.user.dto.UserResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
           @Valid @RequestBody UserCreateRequest request
    ) {
        return userService.createUser(request);
    }
}
