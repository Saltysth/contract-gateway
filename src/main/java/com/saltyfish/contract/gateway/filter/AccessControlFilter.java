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
 * 访问控制过滤器，实现API黑白名单控制
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

        try {
            // 检查访问权限
            boolean allowed = accessControlService.isAccessAllowed(path, method, clientIp, null);
            
            if (!allowed) {
                log.warn("访问被拒绝: path={}, method={}, clientIp={}", path, method, clientIp);
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }

            log.debug("访问允许: path={}, method={}, clientIp={}", path, method, clientIp);
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("访问控制检查异常: path={}, method={}, clientIp={}", path, method, clientIp, e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
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
        // 设置较高优先级，在其他过滤器之前执行
        return -100;
    }
}