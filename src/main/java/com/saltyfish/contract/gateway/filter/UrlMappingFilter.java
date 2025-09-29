package com.saltyfish.contract.gateway.filter;

import com.saltyfish.contract.gateway.entity.UrlMapping;
import com.saltyfish.contract.gateway.service.UrlMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * URL Mapping Filter
 * URL映射过滤器，实现路径重写功能
 */
@Slf4j
@Component
public class UrlMappingFilter implements GlobalFilter, Ordered {

    @Autowired
    private UrlMappingService urlMappingService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String originalPath = request.getURI().getPath();

        try {
            // 查找URL映射
            UrlMapping mapping = urlMappingService.findMapping(originalPath);
            if (mapping == null) {
                log.debug("未找到URL映射，使用原始路径: {}", originalPath);
                return chain.filter(exchange);
            }

            // 重写路径
            String newPath = urlMappingService.rewritePath(originalPath, mapping);
            if (newPath.equals(originalPath)) {
                log.debug("路径未发生变化: {}", originalPath);
                return chain.filter(exchange);
            }

            // 创建新的请求URI
            URI newUri = request.getURI().resolve(newPath);
            ServerHttpRequest modifiedRequest = request.mutate()
                    .uri(newUri)
                    .header("X-Original-Path", originalPath)
                    .header("X-Target-Service", mapping.getTargetService())
                    .build();

            log.debug("URL映射完成: {} -> {}, 目标服务: {}", originalPath, newPath, mapping.getTargetService());

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("URL映射处理异常: originalPath={}", originalPath, e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        // 在用户信息过滤器之后，路由过滤器之前执行
        return -80;
    }
}