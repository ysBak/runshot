
package com.runshot.auth.jwt;

import com.runshot.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Authorization 헤더 확인
        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null
                && authorization.startsWith("Bearer ")) {

            // 2. Bearer 접두사를 제거하고 JWT 추출
            String token = authorization.substring(7).trim();

            try {
                // 3. JWT 검증 후 사용자 ID 추출
                Long userId = jwtTokenProvider.getUserId(token);

                // 4. 사용자가 DB에 존재하는지 확인
                if (userRepository.existsById(userId)) {

                    // 5. Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.emptyList()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // 6. 현재 요청에 인증 정보 등록
                    SecurityContext context =
                            SecurityContextHolder.createEmptyContext();

                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                }

            } catch (JwtException | IllegalArgumentException e) {
                // 유효하지 않은 토큰은 인증하지 않음
                SecurityContextHolder.clearContext();
            }
        }

        // 7. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
