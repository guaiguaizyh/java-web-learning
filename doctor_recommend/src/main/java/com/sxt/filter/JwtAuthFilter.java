package com.sxt.filter;

import com.sxt.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    public JwtAuthFilter(JwtUtils jwtUtils, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 获取请求路径
        String requestPath = request.getRequestURI();
        log.info("Processing request: {}", requestPath);
        
        // 检查是否为公开接口路径，如果是直接放行
        if (requestPath.equals("/login") || 
            requestPath.equals("/register") || 
            requestPath.equals("/register/check-username")) {
            log.info("Skipping JWT validation for public API: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. 检查 Authorization Header 是否存在且格式正确 (Bearer <token>)
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 提取 JWT
        jwt = authHeader.substring(7);
        try {
            // 3. 从 JWT 中提取用户名
            username = jwtUtils.extractUsername(jwt);
            // 4. 检查用户名是否存在，并且当前 SecurityContext 中没有认证信息
            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. 加载 UserDetails
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // 6. 验证 Token 是否有效
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    // 7. 创建认证 Token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    // 8. 设置认证详情
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // 9. 更新 SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    if (log.isDebugEnabled()) {
                        log.debug("JWT Filter: Set Authentication in SecurityContext for user '{}' with authorities: {}",
                                userDetails.getUsername(), userDetails.getAuthorities());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JWT Token processing error: {}", e.getMessage(), e);
        }

        // 10. 继续过滤器链
        filterChain.doFilter(request, response);
    }
} 