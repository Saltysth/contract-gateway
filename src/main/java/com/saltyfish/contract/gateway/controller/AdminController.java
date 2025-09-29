package com.saltyfish.contract.gateway.controller;

import com.saltyfish.contract.gateway.service.AccessControlService;
import com.saltyfish.contract.gateway.service.UrlMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}