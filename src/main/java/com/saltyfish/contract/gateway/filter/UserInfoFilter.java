package com.saltyfish.contract.gateway.filter;

import com.saltyfish.contract.gateway.dto.UserInfo;
import com.saltyfish.contract.gateway.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * User Info Filter
 * 用户信息过滤器，解析JWT Token并将用户信息传递给后端服务
 */
@Slf4j
@Component
public class UserInfoFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            log.debug("请求中没有Authorization头: {}", request.getURI().getPath());
            return chain.filter(exchange);
        }

        try {
            // 解析JWT Token
            UserInfo userInfo = jwtService.parseToken(authHeader);
            if (userInfo == null) {
                log.debug("JWT Token解析失败: {}", request.getURI().getPath());
                return chain.filter(exchange);
            }

            // 将用户信息添加到请求头中传递给后端服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userInfo.getUserId())
                    .header("X-Username", userInfo.getUsername())
                    .header("X-User-Email", userInfo.getEmail())
                    .header("X-Tenant-Id", userInfo.getTenantId())
                    .header("X-User-Roles", String.join(",", userInfo.getRoles() != null ? userInfo.getRoles() : java.util.List.of()))
                    .build();

            log.debug("用户信息已添加到请求头: userId={}, username={}", userInfo.getUserId(), userInfo.getUsername());

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("用户信息处理异常: {}", request.getURI().getPath(), e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        // 在访问控制过滤器之后执行
        return -90;
    }
}