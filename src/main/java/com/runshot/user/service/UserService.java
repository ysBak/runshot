package com.runshot.user.service;

import com.runshot.user.dto.UserCreateRequest;
import com.runshot.user.dto.UserResponse;
import com.runshot.user.entity.User;
import com.runshot.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createUser(
            UserCreateRequest request
    ) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }

        User user = new User(
                request.email(),
                request.password(),
                request.nickname()
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }
}