package com.saltyfish.contract.gateway.service;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Discovery Service
 * 服务发现服务，提供服务实例的查询和管理功能
 */
@Slf4j
@Service
public class DiscoveryService {

    @Autowired
    private NamingService namingService;

    /**
     * 获取服务的所有实例
     *
     * @param serviceName    服务名称
     * @param groupName 分组名称
     * @return 服务实例列表
     */
    public List<Instance> getAllInstances(String serviceName, String groupName) {
        try {
            List<Instance> instances = namingService.getAllInstances(serviceName, groupName);
            log.debug("获取服务实例成功: serviceName={}, instanceCount={}", serviceName, instances.size());
            return instances;
        } catch (Exception e) {
            log.error("获取服务实例失败: serviceName={}", serviceName, e);
            return null;
        }
    }

    /**
     * 获取服务的健康实例
     *
     * @param serviceName 服务名称
     * @return 健康的服务实例列表
     */
    public List<Instance> getHealthyInstances(String serviceName) {
        try {
            List<Instance> instances = namingService.selectInstances(serviceName, true);
            log.debug("获取健康服务实例成功: serviceName={}, healthyInstanceCount={}", serviceName, instances.size());
            return instances;
        } catch (Exception e) {
            log.error("获取健康服务实例失败: serviceName={}", serviceName, e);
            return null;
        }
    }

    /**
     * 根据负载均衡算法选择一个服务实例
     *
     * @param serviceName 服务名称
     * @return 选中的服务实例
     */
    public Instance selectOneHealthyInstance(String serviceName) {
        try {
            Instance instance = namingService.selectOneHealthyInstance(serviceName);
            if (instance != null) {
                log.debug("选择服务实例成功: serviceName={}, instance={}:{}", 
                         serviceName, instance.getIp(), instance.getPort());
            } else {
                log.warn("没有可用的健康服务实例: serviceName={}", serviceName);
            }
            return instance;
        } catch (Exception e) {
            log.error("选择服务实例失败: serviceName={}", serviceName, e);
            return null;
        }
    }

    /**
     * 检查服务是否存在健康实例
     *
     * @param serviceName 服务名称
     * @return 是否存在健康实例
     */
    public boolean hasHealthyInstance(String serviceName) {
        List<Instance> instances = getHealthyInstances(serviceName);
        return instances != null && !instances.isEmpty();
    }

    /**
     * 获取服务实例的URL
     *
     * @param instance 服务实例
     * @param useHttps 是否使用HTTPS
     * @return 服务实例URL
     */
    public String getInstanceUrl(Instance instance, boolean useHttps) {
        if (instance == null) {
            return null;
        }
        String protocol = useHttps ? "https" : "http";
        return String.format("%s://%s:%d", protocol, instance.getIp(), instance.getPort());
    }

    /**
     * 获取服务实例的URL（默认使用HTTP）
     *
     * @param instance 服务实例
     * @return 服务实例URL
     */
    public String getInstanceUrl(Instance instance) {
        return getInstanceUrl(instance, false);
    }
}