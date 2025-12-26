package com.saltyfish.contract.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security Configuration
 * 网关安全配置，禁用默认的Spring Security认证，允许所有请求通过
 * 网关层的认证由AccessControlFilter（黑白名单）和下游服务处理
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // 禁用CSRF保护（API网关不需要）
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // 禁用CORS（使用网关自己的CorsWebFilter处理）
                .cors(ServerHttpSecurity.CorsSpec::disable)
                // 禁用HTTP Basic认证
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                // 禁用表单登录
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // 允许所有请求通过（网关不做统一认证，由下游服务处理）
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .build();
    }
}
