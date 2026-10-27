package com.runshot.auth.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Sites가 아닌 경로에서 비공개 백엔드 API에 직접 접근하는 것을 방지한다. */
public class GatewayKeyFilter extends OncePerRequestFilter {
    private final byte[] key;
    public GatewayKeyFilter(String key) {
        this.key = key.getBytes(StandardCharsets.UTF_8);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (key.length > 0 && request.getServletPath().startsWith("/api/")) {
            String supplied = request.getHeader("X-Runshot-Gateway-Key");
            if (supplied == null || !MessageDigest.isEqual(key, supplied.getBytes(StandardCharsets.UTF_8))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\":\"접근 권한이 없습니다.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
