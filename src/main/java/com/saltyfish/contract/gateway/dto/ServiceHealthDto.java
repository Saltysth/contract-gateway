package com.saltyfish.contract.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务健康状态DTO
 * 简化的服务健康状态响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthDto {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务状态
     * HEALTHY: 全部实例健康
     * AVAILABLE: 大于等于一半实例健康
     * DANGER: 小于一半实例健康
     * UNAVAILABLE: 无实例健康
     */
    private String status;

    /**
     * 健康实例数量
     */
    private int healthyCount;

    /**
     * 不健康实例数量
     */
    private int unhealthyCount;

    /**
     * 实例总数
     */
    private int totalInstance;
}