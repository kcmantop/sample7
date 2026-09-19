package com.example.ticketing.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

//        if (StringUtils.hasText(token)) {
//        	if(jwtProvider.validateToken(token)) {        
//	            Long userId = jwtProvider.getUserId(token);
//	            String role = jwtProvider.getRole(token);
//	
//	            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//	                    userId,
//	                    null,
//	                    Collections.singletonList(new SimpleGrantedAuthority(role))
//	            );
//	            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//	
//	            SecurityContextHolder.getContext().setAuthentication(authentication);
//        	}
//        	else {
//        		System.out.println("[DEBUG] JWT 토큰 검증 실패: 토큰이 만료되었거나 서명이 유효하지 않음");
//        	}
//        }
//        else {
//        	System.out.println("[DEBUG] Authorization 헤더에 토큰이 존재하지 않음");
//        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // ⭕ 아래 경로로 들어오는 요청은 JWT 검증 필터를 건너뜀
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/reservations") || 
               path.startsWith("/reservation/");
    }
}