package com.saltyfish.contract.gateway.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * Nacos Config Listener
 * Nacos配置监听器，用于监听配置变更并动态更新
 */
@Slf4j
@Component
@RefreshScope
public class NacosConfigListener {

    @Autowired
    private ConfigService configService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.cloud.nacos.config.group:CONTRACT_REVIEW}")
    private String configGroup;

    @Value("${spring.cloud.nacos.config.namespace:dev}")
    private String namespace;

    /**
     * 初始化配置监听器
     */
    @PostConstruct
    public void initConfigListener() {
        try {
            // 监听黑白名单配置
            addAccessRuleConfigListener();
            
            // 监听URL映射配置
            addUrlMappingConfigListener();
            
            // 监听路由配置
            addRouteConfigListener();
            
            log.info("Nacos配置监听器初始化完成");
        } catch (Exception e) {
            log.error("初始化Nacos配置监听器失败", e);
        }
    }

    /**
     * 添加访问规则配置监听器
     */
    private void addAccessRuleConfigListener() throws NacosException {
        String dataId = "contract-gateway-access-rules.yml";
        
        configService.addListener(dataId, configGroup, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("接收到访问规则配置更新: {}", configInfo);
                // TODO: 实现访问规则配置的动态更新逻辑
                handleAccessRuleConfigChange(configInfo);
            }
        });
        
        log.info("已添加访问规则配置监听器: {}", dataId);
    }

    /**
     * 添加URL映射配置监听器
     */
    private void addUrlMappingConfigListener() throws NacosException {
        String dataId = "contract-gateway-url-mappings.yml";
        
        configService.addListener(dataId, configGroup, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("接收到URL映射配置更新: {}", configInfo);
                // TODO: 实现URL映射配置的动态更新逻辑
                handleUrlMappingConfigChange(configInfo);
            }
        });
        
        log.info("已添加URL映射配置监听器: {}", dataId);
    }

    /**
     * 添加路由配置监听器
     */
    private void addRouteConfigListener() throws NacosException {
        String dataId = "contract-gateway-routes.yml";
        
        configService.addListener(dataId, configGroup, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("接收到路由配置更新: {}", configInfo);
                // TODO: 实现路由配置的动态更新逻辑
                handleRouteConfigChange(configInfo);
            }
        });
        
        log.info("已添加路由配置监听器: {}", dataId);
    }

    /**
     * 处理访问规则配置变更
     */
    private void handleAccessRuleConfigChange(String configInfo) {
        try {
            // TODO: 解析配置并更新访问规则缓存
            log.info("访问规则配置已更新");
        } catch (Exception e) {
            log.error("处理访问规则配置变更失败", e);
        }
    }

    /**
     * 处理URL映射配置变更
     */
    private void handleUrlMappingConfigChange(String configInfo) {
        try {
            // TODO: 解析配置并更新URL映射缓存
            log.info("URL映射配置已更新");
        } catch (Exception e) {
            log.error("处理URL映射配置变更失败", e);
        }
    }

    /**
     * 处理路由配置变更
     */
    private void handleRouteConfigChange(String configInfo) {
        try {
            // TODO: 解析配置并动态更新路由规则
            log.info("路由配置已更新");
        } catch (Exception e) {
            log.error("处理路由配置变更失败", e);
        }
    }
}