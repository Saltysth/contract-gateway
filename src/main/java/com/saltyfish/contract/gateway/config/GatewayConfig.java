package com.saltyfish.contract.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Configuration
 * 网关路由配置
 */
@Configuration
public class GatewayConfig {

    /**
     * 配置路由规则
     * TODO: 实现动态路由配置，支持从Nacos配置中心读取路由规则
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // TODO: 添加具体的路由规则
                .build();
    }
}