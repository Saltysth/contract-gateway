package com.saltyfish.contract.gateway.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class NacosConfig {

    // 从配置文件读取 Nacos 服务器地址
    @Value("${spring.cloud.nacos.server-addr}")
    private String serverAddr;

    // 从配置文件读取命名空间
    @Value("${spring.cloud.nacos.namespace:public}")
    private String namespace;

    // 从配置文件读取用户名
    @Value("${spring.cloud.nacos.username:nacos}")
    private String username;

    // 从配置文件读取密码
    @Value("${spring.cloud.nacos.password:nacos}")
    private String password;

    /**
     * 注册 ConfigService 实例
     */
    @Bean
    public ConfigService configService() throws NacosException {
        Properties properties = getNacosProperties();
        return NacosFactory.createConfigService(properties);
    }

    /**
     * 注册 NamingService 实例（新增部分）
     */
    @Bean
    public NamingService namingService() throws NacosException {
        Properties properties = getNacosProperties();
        return NacosFactory.createNamingService(properties);
    }

    /**
     * 抽取共用的 Nacos 配置属性
     */
    private Properties getNacosProperties() {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespace);
        properties.put("username", username);
        properties.put("password", password);
        return properties;
    }
}