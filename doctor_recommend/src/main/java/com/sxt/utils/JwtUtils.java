package com.sxt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtils {

    // 从配置文件读取密钥，确保足够复杂且保密
    @Value("${jwt.secret:fallbackSecretKeyForDevelopmentEnvironmentOnly}")
    private String secretString;

    // 从配置文件读取过期时间 (毫秒)，例如 24小时
    @Value("${jwt.expirationMs:86400000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        // 确保密钥长度至少为 32 字节 (256 位)，满足 HS256 的要求
        byte[] keyBytes = secretString.getBytes();
        if (keyBytes.length < 32) {
            // 如果密钥长度不足，使用 SHA-256 进行哈希
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to generate secure key", e);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 从 Token 中提取用户名
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 从 Token 中提取过期日期
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 从 Token 中提取指定的 Claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 解析 Token 中的所有 Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 检查 Token 是否过期
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 为指定用户生成 Token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 你可以在这里添加额外的 claims，比如角色
        // claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }
        
    // 创建 Token
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // subject 通常是用户名
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 使用 HS256 签名算法
                .compact();
    }

    // 验证 Token 是否有效
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
} 