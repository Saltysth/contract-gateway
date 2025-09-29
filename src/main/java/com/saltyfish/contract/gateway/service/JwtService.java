package com.saltyfish.contract.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltyfish.contract.gateway.config.JwtConfig;
import com.saltyfish.contract.gateway.dto.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT Service
 * JWT服务，提供Token解析和用户信息提取功能
 */
@Slf4j
@Service
public class JwtService {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 解析JWT Token并提取用户信息
     *
     * @param token JWT Token
     * @return 用户信息
     */
    public UserInfo parseToken(String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 创建密钥
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

            // 解析Token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // 提取用户信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(claims.getSubject());
            userInfo.setUsername(claims.get("username", String.class));
            userInfo.setEmail(claims.get("email", String.class));
            userInfo.setTenantId(claims.get("tenantId", String.class));

            // 提取角色信息
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            userInfo.setRoles(roles);

            // 提取扩展属性
            Object extensions = claims.get("extensions");
            userInfo.setExtensions(extensions);

            log.debug("JWT Token解析成功: userId={}, username={}", userInfo.getUserId(), userInfo.getUsername());
            return userInfo;

        } catch (Exception e) {
            log.warn("JWT Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            UserInfo userInfo = parseToken(token);
            return userInfo != null;
        } catch (Exception e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public String extractUserId(String token) {
        UserInfo userInfo = parseToken(token);
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String extractUsername(String token) {
        UserInfo userInfo = parseToken(token);
        return userInfo != null ? userInfo.getUsername() : null;
    }
}