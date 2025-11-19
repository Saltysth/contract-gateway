package com.saltyfish.contract.gateway.controller;

import com.saltyfish.contract.gateway.service.AccessControlService;
import com.saltyfish.contract.gateway.service.UrlMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Admin Controller
 * 管理控制器，提供缓存管理和系统管理接口
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private UrlMappingService urlMappingService;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    /**
     * 刷新访问规则缓存
     */
    @PostMapping("/cache/access-rules/refresh")
    public ResponseEntity<String> refreshAccessRulesCache() {
        try {
            accessControlService.refreshAccessRulesCache();
            return ResponseEntity.ok("访问规则缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新访问规则缓存失败", e);
            return ResponseEntity.internalServerError().body("刷新失败: " + e.getMessage());
        }
    }

    /**
     * 清除访问规则缓存
     */
    @DeleteMapping("/cache/access-rules")
    public ResponseEntity<String> clearAccessRulesCache() {
        try {
            accessControlService.clearAccessRulesCache();
            return ResponseEntity.ok("访问规则缓存清除成功");
        } catch (Exception e) {
            log.error("清除访问规则缓存失败", e);
            return ResponseEntity.internalServerError().body("清除失败: " + e.getMessage());
        }
    }

    /**
     * 刷新URL映射缓存
     */
    @PostMapping("/cache/url-mappings/refresh")
    public ResponseEntity<String> refreshUrlMappingsCache() {
        try {
            urlMappingService.refreshUrlMappingsCache();
            return ResponseEntity.ok("URL映射缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新URL映射缓存失败", e);
            return ResponseEntity.internalServerError().body("刷新失败: " + e.getMessage());
        }
    }

    /**
     * 清除URL映射缓存
     */
    @DeleteMapping("/cache/url-mappings")
    public ResponseEntity<String> clearUrlMappingsCache() {
        try {
            urlMappingService.clearUrlMappingsCache();
            return ResponseEntity.ok("URL映射缓存清除成功");
        } catch (Exception e) {
            log.error("清除URL映射缓存失败", e);
            return ResponseEntity.internalServerError().body("清除失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Gateway is healthy");
    }

    /**
     * 系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Object> info() {
        // TODO: 返回系统信息，如版本、启动时间、配置信息等
        return ResponseEntity.ok("Gateway system info");
    }

    // ==================== 路由管理接口 ====================

    /**
     * 获取所有路由定义
     */
    @GetMapping("/routes")
    public Flux<RouteDefinition> getAllRoutes() {
        try {
            return routeDefinitionLocator.getRouteDefinitions()
                    .doOnNext(route -> log.debug("获取路由: {} -> {}", route.getId(), route.getUri()));
        } catch (Exception e) {
            log.error("获取路由定义失败", e);
            return Flux.empty();
        }
    }

    /**
     * 根据ID获取路由定义
     */
    @GetMapping("/routes/{id}")
    public Mono<ResponseEntity<RouteDefinition>> getRouteById(@PathVariable String id) {
        try {
            return routeDefinitionLocator.getRouteDefinitions()
                    .filter(route -> route.getId().equals(id))
                    .next()
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("获取路由定义失败: routeId={}", id, e);
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }

    /**
     * 刷新路由配置（从Nacos重新加载）
     * TODO: 实现路由配置的动态刷新功能
     */
    @PostMapping("/routes/refresh")
    public ResponseEntity<String> refreshRoutes() {
        try {
            // TODO: 调用DynamicRouteConfig的路由刷新方法
            log.info("手动触发路由配置刷新");
            return ResponseEntity.ok("路由配置刷新成功（功能待实现）");
        } catch (Exception e) {
            log.error("刷新路由配置失败", e);
            return ResponseEntity.internalServerError().body("刷新失败: " + e.getMessage());
        }
    }

    /**
     * 获取路由统计信息
     */
    @GetMapping("/routes/stats")
    public ResponseEntity<Object> getRouteStats() {
        try {
            // TODO: 实现路由统计功能，记录每个路由的调用次数、响应时间等
            return ResponseEntity.ok(java.util.Map.of(
                "totalRoutes", "待实现",
                "activeRoutes", "待实现",
                "lastUpdated", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("获取路由统计信息失败", e);
            return ResponseEntity.internalServerError().body("获取统计信息失败");
        }
    }
}