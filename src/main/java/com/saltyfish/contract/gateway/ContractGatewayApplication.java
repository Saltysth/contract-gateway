package com.saltyfish.contract.gateway;

import com.ruoyi.feign.config.RemoteAuthWebFluxAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * Contract Gateway Application
 * API网关服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ruoyi.feign.service")
@Import(RemoteAuthWebFluxAutoConfiguration.class)  // 只导入WebFlux配置
public class ContractGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractGatewayApplication.class, args);
    }
}