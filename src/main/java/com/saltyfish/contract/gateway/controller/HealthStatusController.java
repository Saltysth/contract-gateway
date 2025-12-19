package com.saltyfish.contract.gateway.controller;

import com.saltyfish.contract.gateway.dto.ServiceHealthDto;
import com.saltyfish.contract.gateway.service.HealthStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微服务健康状态控制器
 * 提供基于 Nacos 的微服务健康状态聚合展示功能
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthStatusController {

    private final HealthStatusService healthStatusService;

    /**
     * 获取所有微服务的健康状态
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getAllServiceHealth() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ServiceHealthDto> serviceList = healthStatusService.getAllServiceHealth();

            result.put("code", 200);
            result.put("message", "");
            result.put("data", serviceList);
            result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        } catch (Exception e) {
            log.error("获取所有服务健康状态失败", e);
            result.put("code", 500);
            result.put("message", "获取服务健康状态失败: " + e.getMessage());
            result.put("data", Collections.emptyList());
            result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取单个服务的健康状态
     */
    @GetMapping("/service")
    public ResponseEntity<Map<String, Object>> getServiceHealth(@RequestParam String serviceName) {
        Map<String, Object> result = new HashMap<>();

        try {
            ServiceHealthDto serviceHealth = healthStatusService.getServiceHealth(serviceName);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", serviceHealth);
            result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        } catch (Exception e) {
            log.error("获取服务健康状态失败: serviceName={}", serviceName, e);
            result.put("code", 500);
            result.put("message", "获取服务健康状态失败: " + e.getMessage());
            result.put("data", null);
            result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return ResponseEntity.ok(result);
    }
}