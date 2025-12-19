package com.saltyfish.contract.gateway.service;

import com.saltyfish.contract.gateway.dto.ServiceHealthDto;

import java.util.List;

/**
 * 健康状态服务接口
 */
public interface HealthStatusService {

    /**
     * 获取所有服务的健康状态
     *
     * @return 服务健康状态列表
     */
    List<ServiceHealthDto> getAllServiceHealth();

    /**
     * 获取指定服务的健康状态
     *
     * @param serviceName 服务名称
     * @return 服务健康状态
     */
    ServiceHealthDto getServiceHealth(String serviceName);
}