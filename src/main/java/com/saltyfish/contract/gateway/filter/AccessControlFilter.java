package com.saltyfish.contract.gateway.filter;

import com.saltyfish.contract.gateway.service.AccessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Access Control Filter
 * 访问控制过滤器，实现基于IP、路径、方法的黑白名单控制（响应式版本）
 *
 * 职责：处理IP黑名单、IP白名单、路径访问限制等
 */
@Slf4j
@Component
public class AccessControlFilter implements GlobalFilter, Ordered {

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 获取请求信息
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String clientIp = getClientIp(request);

        log.debug("访问控制检查: path={}, method={}, clientIp={}", path, method, clientIp);

        // 检查访问权限（IP黑白名单、路径限制等）
        return accessControlService.isAccessAllowed(path, method, clientIp, null)
                .flatMap(allowed -> {
                    if (!allowed) {
                        log.warn("访问被拒绝: path={}, method={}, clientIp={}", path, method, clientIp);
                        response.setStatusCode(HttpStatus.FORBIDDEN);
                        return response.setComplete();
                    }

                    log.debug("访问允许: path={}, method={}, clientIp={}", path, method, clientIp);
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("访问控制检查异常: path={}, method={}, clientIp={}", path, method, clientIp, e);
                    // 访问控制异常时拒绝访问（fail-closed 安全策略）
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return response.setComplete();
                });
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
               request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        // 设置较高优先级，最先执行访问控制检查
        return -100;
    }
}