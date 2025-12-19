package com.saltyfish.contract.gateway.service.impl;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.saltyfish.contract.gateway.dto.ServiceHealthDto;
import com.saltyfish.contract.gateway.service.DiscoveryService;
import com.saltyfish.contract.gateway.service.HealthStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 健康状态服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthStatusServiceImpl implements HealthStatusService {

    private final DiscoveryService discoveryService;
    private final NamingService namingService;

    // 需要监控的服务列表
    private static final List<String> MONITORED_SERVICES = Arrays.asList(
        "contract-gateway",
        "contract-management-service",
        "contract-review-engine",
        "contract-file-storage-service",
        "contract-ai-service"
    );

    @Override
    public List<ServiceHealthDto> getAllServiceHealth() {
        List<ServiceHealthDto> serviceList = new ArrayList<>();

        try {
            // 1. 获取所有服务名
            List<String> allServices = namingService.getServicesOfServer(0, 100, "CONTRACT_REVIEW").getData();

            // 2. 遍历每个服务，获取实例健康状态
            for (String serviceName : allServices) {
                // 只监控我们关心的服务
                if (!MONITORED_SERVICES.contains(serviceName)) {
                    continue;
                }

                ServiceHealthDto serviceHealth = getServiceHealthInternal(serviceName);
                if (serviceHealth != null) {
                    serviceList.add(serviceHealth);
                }
            }
        } catch (Exception e) {
            log.error("获取所有服务健康状态失败", e);
        }

        return serviceList;
    }

    @Override
    public ServiceHealthDto getServiceHealth(String serviceName) {
        return getServiceHealthInternal(serviceName);
    }

    /**
     * 内部方法：获取单个服务的健康状态
     *
     * @param serviceName 服务名称
     * @return 服务健康状态DTO
     */
    private ServiceHealthDto getServiceHealthInternal(String serviceName) {
        try {
            // 获取该服务的所有实例
            List<Instance> instances = discoveryService.getAllInstances(serviceName, "CONTRACT_REVIEW");

            if (instances == null || instances.isEmpty()) {
                return ServiceHealthDto.builder()
                        .serviceName(serviceName)
                        .status("UNAVAILABLE")
                        .healthyCount(0)
                        .unhealthyCount(0)
                        .totalInstance(0)
                        .build();
            }

            // 统计健康/不健康实例数
            long healthyCount = instances.stream()
                    .filter(Instance::isHealthy)
                    .count();
            long unhealthyCount = instances.size() - healthyCount;

            // 计算服务状态
            String status = calculateServiceStatus((int) healthyCount, (int) unhealthyCount, instances.size());

            return ServiceHealthDto.builder()
                    .serviceName(serviceName)
                    .status(status)
                    .healthyCount((int) healthyCount)
                    .unhealthyCount((int) unhealthyCount)
                    .totalInstance(instances.size())
                    .build();

        } catch (Exception e) {
            log.error("获取服务健康状态失败: serviceName={}", serviceName, e);
            // 对于获取失败的服务，记录为不可用
            return ServiceHealthDto.builder()
                    .serviceName(serviceName)
                    .status("UNAVAILABLE")
                    .healthyCount(0)
                    .unhealthyCount(0)
                    .totalInstance(0)
                    .build();
        }
    }

    /**
     * 计算服务状态
     *
     * @param healthyCount 健康实例数
     * @param unhealthyCount 不健康实例数
     * @param totalCount 总实例数
     * @return 服务状态
     */
    private String calculateServiceStatus(int healthyCount, int unhealthyCount, int totalCount) {
        if (totalCount == 0) {
            return "UNAVAILABLE";
        }

        // HEALTHY：全部实例健康
        if (healthyCount == totalCount) {
            return "HEALTHY";
        }

        // UNAVAILABLE：无实例健康
        if (healthyCount == 0) {
            return "UNAVAILABLE";
        }

        // AVAILABLE：大于等于一半实例健康，小数四舍五入
        // 计算是否大于等于一半，考虑四舍五入
        if (healthyCount >= (totalCount + 1) / 2.0) {
            return "AVAILABLE";
        }

        // DANGER：小于一半实例健康，小数四舍五入
        return "DANGER";
    }
}