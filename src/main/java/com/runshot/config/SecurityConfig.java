
package com.runshot.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.DispatcherType;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // 1. JWT 기반 REST API를 위한 CSRF 설정
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 서버 세션을 사용하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. 기본 로그인 화면 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // 4. HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // 5. 인증되지 않은 요청에 401 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                        )
                )

                // 6. API별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 오류 처리 요청은 인증 없이 허용
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // 회원가입 허용
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        // 로그인 허용
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
