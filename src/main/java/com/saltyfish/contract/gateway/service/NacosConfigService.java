package com.saltyfish.contract.gateway.service;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Nacos Config Service
 * Nacos配置服务，提供配置的读取和发布功能
 */
@Slf4j
@Service
public class NacosConfigService {

    @Autowired
    private ConfigService configService;

    @Value("${spring.cloud.nacos.config.group:CONTRACT_REVIEW}")
    private String configGroup;

    @Value("${spring.cloud.nacos.config.namespace:dev}")
    private String namespace;

    /**
     * 获取配置内容
     *
     * @param dataId 配置ID
     * @return 配置内容
     */
    public String getConfig(String dataId) {
        try {
            String config = configService.getConfig(dataId, configGroup, 5000);
            log.debug("获取配置成功: dataId={}, group={}, config={}", dataId, configGroup, config);
            return config;
        } catch (NacosException e) {
            log.error("获取配置失败: dataId={}, group={}", dataId, configGroup, e);
            return null;
        }
    }

    /**
     * 发布配置
     *
     * @param dataId  配置ID
     * @param content 配置内容
     * @return 是否发布成功
     */
    public boolean publishConfig(String dataId, String content) {
        try {
            boolean result = configService.publishConfig(dataId, configGroup, content);
            if (result) {
                log.info("发布配置成功: dataId={}, group={}", dataId, configGroup);
            } else {
                log.warn("发布配置失败: dataId={}, group={}", dataId, configGroup);
            }
            return result;
        } catch (NacosException e) {
            log.error("发布配置异常: dataId={}, group={}", dataId, configGroup, e);
            return false;
        }
    }

    /**
     * 删除配置
     *
     * @param dataId 配置ID
     * @return 是否删除成功
     */
    public boolean removeConfig(String dataId) {
        try {
            boolean result = configService.removeConfig(dataId, configGroup);
            if (result) {
                log.info("删除配置成功: dataId={}, group={}", dataId, configGroup);
            } else {
                log.warn("删除配置失败: dataId={}, group={}", dataId, configGroup);
            }
            return result;
        } catch (NacosException e) {
            log.error("删除配置异常: dataId={}, group={}", dataId, configGroup, e);
            return false;
        }
    }

    /**
     * 获取访问规则配置
     */
    public String getAccessRuleConfig() {
        return getConfig("contract-gateway-access-rules.yml");
    }

    /**
     * 获取URL映射配置
     */
    public String getUrlMappingConfig() {
        return getConfig("contract-gateway-url-mappings.yml");
    }

    /**
     * 获取路由配置
     */
    public String getRouteConfig() {
        return getConfig("contract-gateway-routes.yml");
    }

    /**
     * 发布访问规则配置
     */
    public boolean publishAccessRuleConfig(String content) {
        return publishConfig("contract-gateway-access-rules.yml", content);
    }

    /**
     * 发布URL映射配置
     */
    public boolean publishUrlMappingConfig(String content) {
        return publishConfig("contract-gateway-url-mappings.yml", content);
    }

    /**
     * 发布路由配置
     */
    public boolean publishRouteConfig(String content) {
        return publishConfig("contract-gateway-routes.yml", content);
    }
}