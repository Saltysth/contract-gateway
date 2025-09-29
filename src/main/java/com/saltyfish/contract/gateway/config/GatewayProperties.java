package com.saltyfish.contract.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gateway Properties
 * 网关配置属性
 */
@Data
@Component("ContractGatewayProperties")
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    /**
     * 访问控制配置
     */
    private AccessControl accessControl = new AccessControl();

    /**
     * URL映射配置
     */
    private UrlMapping urlMapping = new UrlMapping();

    /**
     * 监控配置
     */
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class AccessControl {
        /**
         * 是否启用访问控制
         */
        private boolean enabled = true;

        /**
         * 默认策略：allow/deny
         */
        private String defaultPolicy = "allow";
    }

    @Data
    public static class UrlMapping {
        /**
         * 是否启用URL映射
         */
        private boolean enabled = true;
    }

    @Data
    public static class Monitoring {
        /**
         * 是否启用监控
         */
        private boolean enabled = true;
    }
}