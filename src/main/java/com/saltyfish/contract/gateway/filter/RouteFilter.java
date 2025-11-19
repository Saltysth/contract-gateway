package com.saltyfish.contract.gateway.filter;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.saltyfish.contract.gateway.service.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Route Filter
 * 路由过滤器，实现服务发现和负载均衡
 *
 * 集成说明：
 * - Spring Cloud Gateway会自动处理application.yml中配置的路由规则
 * - 此过滤器主要用于处理需要动态路由的特殊场景
 * - 与访问控制、URL映射等过滤器协同工作
 */
@Slf4j
@Component
public class RouteFilter implements GlobalFilter, Ordered {

    @Autowired
    private DiscoveryService discoveryService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();

        log.debug("RouteFilter处理请求: {}", path);

        // 检查是否已被Spring Cloud Gateway自动路由
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null) {
            log.debug("请求已被Gateway自动路由: {} -> {}", path, route.getUri());
            // TODO: 与访问控制过滤器集成，验证路由后的服务权限
            // TODO: 与监控过滤器集成，记录路由转发统计
            return chain.filter(exchange);
        }

        // 获取手动指定的目标服务名（兼容原有逻辑）
        String targetService = request.getHeaders().getFirst("X-Target-Service");
        if (targetService == null || targetService.isEmpty()) {
            log.debug("未指定目标服务，使用Gateway默认路由: {}", path);
            // TODO: 与URL映射过滤器集成，检查是否需要路径转换
            return chain.filter(exchange);
        }

        try {
            // 手动路由逻辑（用于特殊场景）
            Instance instance = discoveryService.selectOneHealthyInstance(targetService);
            if (instance == null) {
                log.warn("未找到健康的服务实例: {}", targetService);
                response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return response.setComplete();
            }

            // 构建目标URI
            String targetUrl = discoveryService.getInstanceUrl(instance);
            URI targetUri = URI.create(targetUrl + request.getURI().getPath());

            // 修改请求URI
            ServerHttpRequest modifiedRequest = request.mutate()
                    .uri(targetUri)
                    .header("X-Forwarded-Host", request.getHeaders().getFirst("Host"))
                    .header("X-Forwarded-Proto", request.getURI().getScheme())
                    .header("X-Forwarded-Port", String.valueOf(request.getURI().getPort()))
                    .header("X-Gateway-Route-Method", "manual")
                    .build();

            log.info("手动路由到服务实例: {} -> {}:{}{}", targetService, instance.getIp(), instance.getPort(), request.getURI().getPath());

            // TODO: 与用户信息过滤器集成，传递用户上下文到目标服务
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("路由处理异常: targetService={}, path={}", targetService, path, e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 在URL映射过滤器之后执行，在监控过滤器之前执行
        return -70;
    }
}