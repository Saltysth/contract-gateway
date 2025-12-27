package com.saltyfish.contract.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Contract Gateway Application
 * API网关服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ruoyi.feign.service")
public class ContractGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractGatewayApplication.class, args);
    }
}