package com.saltyfish.contract.gateway.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 动态路由配置类
 * 支持从Nacos配置中心动态加载和更新路由规则
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DynamicRouteConfig {

    private final ConfigService configService;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private final ObjectMapper objectMapper;

    /**
     * Nacos路由配置ID
     */
    private static final String ROUTE_DATA_ID = "contract-gateway-routes.yml";
    private static final String ROUTE_GROUP = "CONTRACT_REVIEW";

    /**
     * 初始化动态路由配置
     */
    @PostConstruct
    public void initDynamicRoute() {
        try {
            // 初始加载路由配置
            loadRoutesFromNacos();

            // 监听配置变化
            configService.addListener(ROUTE_DATA_ID, ROUTE_GROUP, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null; // 使用默认线程池
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("收到路由配置更新，开始刷新路由...");
                    refreshRoutes(configInfo);
                }
            });

            log.info("动态路由配置初始化完成");
        } catch (NacosException e) {
            log.error("初始化动态路由配置失败", e);
        }
    }

    /**
     * 从Nacos加载路由配置
     */
    private void loadRoutesFromNacos() {
        try {
            String configInfo = configService.getConfig(ROUTE_DATA_ID, ROUTE_GROUP, 5000);
            if (configInfo != null && !configInfo.trim().isEmpty()) {
                refreshRoutes(configInfo);
                log.info("成功从Nacos加载路由配置");
            } else {
                log.warn("Nacos中未找到路由配置，使用本地配置");
            }
        } catch (NacosException e) {
            log.error("从Nacos加载路由配置失败", e);
        }
    }

    /**
     * 刷新路由配置
     */
    private void refreshRoutes(String configInfo) {
        // 解析YAML配置中的路由定义
        List<RouteDefinition> routeDefinitions = parseRouteDefinitions(configInfo);

        if (!CollectionUtils.isEmpty(routeDefinitions)) {
            log.info("准备更新{}个路由定义", routeDefinitions.size());

            // 使用reactor的非阻塞方式处理路由更新
            clearConfiguredRoutes()
                .thenMany(Flux.fromIterable(routeDefinitions))
                .flatMap(routeDefinition -> {
                    log.debug("添加路由: {} -> {}", routeDefinition.getId(), routeDefinition.getUri());
                    return routeDefinitionWriter.save(Mono.just(routeDefinition));
                })
                .collectList()
                .doOnSuccess(v -> log.info("路由配置更新完成"))
                .doOnError(e -> log.error("刷新路由配置失败", e))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        }
    }

    /**
     * 清除通过配置管理的路由
     * @return Mono完成信号
     */
    private Mono<Void> clearConfiguredRoutes() {
        return routeDefinitionLocator.getRouteDefinitions()
            .filter(route -> route.getMetadata() != null
                && "nacos".equals(route.getMetadata().get("source")))
            .flatMap(route -> {
                log.debug("删除路由: {}", route.getId());
                return routeDefinitionWriter.delete(Mono.just(route.getId()));
            })
            .then()
            .doOnError(e -> log.error("清除路由配置失败", e));
    }

    /**
     * 解析路由定义
     * TODO: 完善YAML解析逻辑，支持复杂的路由配置
     */
    private List<RouteDefinition> parseRouteDefinitions(String configInfo) {
        try {
            // 简单实现：这里需要根据YAML格式进行解析
            // 当前先返回空列表，使用application.yml中的配置
            log.info("解析路由配置: {}", configInfo.substring(0, Math.min(100, configInfo.length())));

            // TODO: 实现完整的YAML到RouteDefinition的转换
            return List.of();
        } catch (Exception e) {
            log.error("解析路由定义失败", e);
            return List.of();
        }
    }
}