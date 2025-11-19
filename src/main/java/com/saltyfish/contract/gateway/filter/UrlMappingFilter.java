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
 *
 * 集成说明：
 * - 与路由过滤器协同工作，先进行URL映射，再进行路由转发
 * - 支持动态URL映射配置，可配合路由规则实现灵活的API网关
 * - 与访问控制过滤器配合，确保映射后的路径仍受权限控制
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

            log.info("URL映射完成: {} -> {}, 目标服务: {}", originalPath, newPath, mapping.getTargetService());

            // TODO: 与路由过滤器集成，确保映射后的路径能正确匹配路由规则
            // TODO: 与访问控制过滤器集成，验证映射后路径的访问权限
            // TODO: 与监控过滤器集成，记录URL映射统计信息
            // TODO: 支持基于路由规则的动态URL映射，减少数据库查询

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