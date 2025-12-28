package com.saltyfish.contract.gateway.controller;

import com.ruoyi.feign.annotation.RemotePreAuthorize;
import com.saltyfish.contract.gateway.service.NacosConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Config Controller
 * 配置管理控制器，提供配置的查询和更新接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/config")
public class ConfigController {

    @Autowired
    private NacosConfigService nacosConfigService;

    /**
     * 获取访问规则配置
     */
    @RemotePreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/access-rules")
    public ResponseEntity<String> getAccessRuleConfig() {
        try {
            String config = nacosConfigService.getAccessRuleConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("获取访问规则配置失败", e);
            return ResponseEntity.internalServerError().body("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新访问规则配置
     */
    @RemotePreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/access-rules")
    public ResponseEntity<String> updateAccessRuleConfig(@RequestBody String config) {
        try {
            boolean success = nacosConfigService.publishAccessRuleConfig(config);
            if (success) {
                return ResponseEntity.ok("访问规则配置更新成功");
            } else {
                return ResponseEntity.internalServerError().body("访问规则配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新访问规则配置失败", e);
            return ResponseEntity.internalServerError().body("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取URL映射配置
     */
    @RemotePreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/url-mappings")
    public ResponseEntity<String> getUrlMappingConfig() {
        try {
            String config = nacosConfigService.getUrlMappingConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("获取URL映射配置失败", e);
            return ResponseEntity.internalServerError().body("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新URL映射配置
     */
    @RemotePreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/url-mappings")
    public ResponseEntity<String> updateUrlMappingConfig(@RequestBody String config) {
        try {
            boolean success = nacosConfigService.publishUrlMappingConfig(config);
            if (success) {
                return ResponseEntity.ok("URL映射配置更新成功");
            } else {
                return ResponseEntity.internalServerError().body("URL映射配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新URL映射配置失败", e);
            return ResponseEntity.internalServerError().body("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取路由配置
     */
    @RemotePreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/routes")
    public ResponseEntity<String> getRouteConfig() {
        try {
            String config = nacosConfigService.getRouteConfig();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("获取路由配置失败", e);
            return ResponseEntity.internalServerError().body("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新路由配置
     */
    @RemotePreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/routes")
    public ResponseEntity<String> updateRouteConfig(@RequestBody String config) {
        try {
            boolean success = nacosConfigService.publishRouteConfig(config);
            if (success) {
                return ResponseEntity.ok("路由配置更新成功");
            } else {
                return ResponseEntity.internalServerError().body("路由配置更新失败");
            }
        } catch (Exception e) {
            log.error("更新路由配置失败", e);
            return ResponseEntity.internalServerError().body("更新配置失败: " + e.getMessage());
        }
    }
}