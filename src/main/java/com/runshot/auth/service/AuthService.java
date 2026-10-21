
package com.runshot.auth.service;

import com.runshot.auth.dto.LoginRequest;
import com.runshot.auth.dto.LoginResponse;
import com.runshot.auth.jwt.JwtTokenProvider;

import com.runshot.user.entity.User;
import com.runshot.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {

        // 1. 이메일로 회원 검색
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "이메일 또는 비밀번호가 올바르지 않습니다."
                ));

        // 2. BCrypt 비밀번호 검증
        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 3. JWT Access Token 발급
        String accessToken =
                jwtTokenProvider.generateToken(user.getId());

        // 4. 만료시간 가져오기
        long expiresIn =
                jwtTokenProvider.getExpirationSeconds();

        // 5. 회원 정보 + JWT 반환
        return LoginResponse.from(
                user,
                accessToken,
                expiresIn
        );
    }
}
