package com.example.ticketing.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class QueueTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private static final String ACTIVE_TOKEN_PREFIX = "token:active:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // HTTP Header에서 토큰 추출
        String token = request.getHeader("X-Queue-Token");

        if (token == null || token.isBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "대기열 토큰이 누락되었습니다.");
            return false;
        }

        // Redis에 활성화된 토큰(Key)이 존재하는지 확인 (토큰 체크)
        String activeKey = ACTIVE_TOKEN_PREFIX + token;
        Boolean hasToken = redisTemplate.hasKey(activeKey);

        if (Boolean.FALSE.equals(hasToken)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "아직 대기 중이거나 만료된 토큰입니다.");
            return false;
        }

        return true; // 검증 성공 시 Controller 진입 허용
    }
}