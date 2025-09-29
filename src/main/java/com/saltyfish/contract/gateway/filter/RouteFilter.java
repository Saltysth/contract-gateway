package com.saltyfish.contract.gateway.filter;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.saltyfish.contract.gateway.service.DiscoveryService;
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

import java.net.URI;

/**
 * Route Filter
 * 路由过滤器，实现服务发现和负载均衡
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

        // 获取目标服务名
        String targetService = request.getHeaders().getFirst("X-Target-Service");
        if (targetService == null || targetService.isEmpty()) {
            log.debug("未指定目标服务，跳过路由处理: {}", request.getURI().getPath());
            return chain.filter(exchange);
        }

        try {
            // 选择健康的服务实例
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
                    .build();

            log.debug("路由到服务实例: {} -> {}:{}", targetService, instance.getIp(), instance.getPort());

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("路由处理异常: targetService={}", targetService, e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 在URL映射过滤器之后执行
        return -70;
    }
}