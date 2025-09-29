# Contract Gateway

合同审查平台API网关服务

## 项目概述

Contract Gateway是基于Spring Cloud Gateway构建的企业级API网关系统，为合同审查平台提供统一的API入口管理、安全控制、监控统计和路由转发功能。

## 核心功能

### 1. API黑白名单控制
- 支持IP、用户、API路径等多维度访问控制
- 提供多层级权限管理和动态规则更新
- 支持精确匹配、前缀匹配、通配符匹配和正则表达式匹配

### 2. API访问统计
- 集成Prometheus + Grafana + Micrometer实现实时监控
- 统计请求量、响应时间、错误率等关键指标
- 支持多维度数据分析和可视化

### 3. URL映射管理
- 提供路径重写、版本映射、服务别名等功能
- 支持动态配置和热更新
- 灵活的URL转换规则

### 4. 智能路由转发
- 基于Nacos服务发现的负载均衡
- 健康检查和故障转移机制
- 支持多种负载均衡算法

### 5. JWT用户身份管理
- JWT Token解析和验证
- 用户信息提取和传递
- 支持多租户架构

## 技术架构

### 技术栈
- **框架**: Spring Boot 2.7.x + Spring Cloud Gateway
- **服务发现**: Nacos Discovery + Nacos Config
- **数据存储**: PostgreSQL + Redis
- **监控**: Micrometer + Prometheus + Grafana
- **安全**: JWT + Spring Security

### 系统架构
```
┌─────────────────────────────────────┐
│            客户端层                  │
├─────────────────────────────────────┤
│            网关层                    │
│  ┌─────────┬─────────┬─────────┐    │
│  │黑白名单 │访问统计 │用户信息 │    │
│  ├─────────┼─────────┼─────────┤    │
│  │URL映射  │路由转发 │过滤器链 │    │
│  └─────────┴─────────┴─────────┘    │
├─────────────────────────────────────┤
│           服务发现层                 │
├─────────────────────────────────────┤
│           微服务层                   │
└─────────────────────────────────────┘
```

## 快速开始

### 环境要求
- JDK 11+
- Maven 3.6+
- PostgreSQL 14+
- Redis 6+
- Nacos 2.x

### 配置说明

#### 1. 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: SaltyFish
```

#### 2. Nacos配置
```yaml
spring:
  cloud:
    nacos:
      server-addr: localhost:18848
      username: SaltyFish
      password: SaltyLin
      discovery:
        namespace: dev
        group: CONTRACT_REVIEW
      config:
        namespace: dev
        group: CONTRACT_REVIEW
        file-extension: yml
```

#### 3. Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 启动步骤

1. **启动依赖服务**
   ```bash
   # 启动PostgreSQL
   # 启动Redis
   # 启动Nacos
   ```

2. **创建数据库表**
   - 应用启动时会自动创建表结构（ddl-auto: update）

3. **配置Nacos**
   - 在Nacos配置中心创建以下配置文件：
     - `contract-gateway-access-rules.yml`
     - `contract-gateway-url-mappings.yml`
     - `contract-gateway-routes.yml`
   - 参考 `src/main/resources/nacos-config-examples/` 目录下的示例配置

4. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

### 配置示例

#### 访问规则配置 (contract-gateway-access-rules.yml)
```yaml
access-rules:
  default-policy: allow
  blacklist:
    - name: "block-admin-api"
      match-type: "path"
      match-pattern: "prefix"
      match-value: "/admin"
      priority: 100
      enabled: true
  whitelist:
    - name: "allow-health-check"
      match-type: "path"
      match-pattern: "exact"
      match-value: "/actuator/health"
      priority: 200
      enabled: true
```

#### URL映射配置 (contract-gateway-url-mappings.yml)
```yaml
url-mappings:
  - name: "api-v1-contracts"
    external-path: "/api/v1/contracts/**"
    internal-path: "/contracts/**"
    target-service: "contract-service"
    mapping-type: "rewrite"
    priority: 100
    enabled: true
```

## API接口

### 管理接口

#### 缓存管理
- `POST /admin/cache/access-rules/refresh` - 刷新访问规则缓存
- `DELETE /admin/cache/access-rules` - 清除访问规则缓存
- `POST /admin/cache/url-mappings/refresh` - 刷新URL映射缓存
- `DELETE /admin/cache/url-mappings` - 清除URL映射缓存

#### 配置管理
- `GET /admin/config/access-rules` - 获取访问规则配置
- `POST /admin/config/access-rules` - 更新访问规则配置
- `GET /admin/config/url-mappings` - 获取URL映射配置
- `POST /admin/config/url-mappings` - 更新URL映射配置

#### 系统管理
- `GET /admin/health` - 健康检查
- `GET /admin/info` - 系统信息

### 监控接口
- `GET /actuator/health` - 应用健康状态
- `GET /actuator/prometheus` - Prometheus指标
- `GET /actuator/gateway/routes` - 网关路由信息

## 监控指标

### 核心指标
- `gateway.requests.total` - 请求总数
- `gateway.requests.duration` - 请求响应时间
- `gateway.requests.errors` - 错误请求数

### 标签维度
- `path` - 请求路径
- `method` - HTTP方法
- `status` - 响应状态码
- `error` - 错误类型

## 部署说明

### Docker部署
```dockerfile
FROM openjdk:11-jre-slim
COPY target/contract-gateway-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 环境变量
- `NACOS_SERVER` - Nacos服务地址
- `NACOS_USERNAME` - Nacos用户名
- `NACOS_PASSWORD` - Nacos密码
- `NACOS_NAMESPACE` - Nacos命名空间
- `DB_USERNAME` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `REDIS_HOST` - Redis主机地址
- `JWT_SECRET` - JWT密钥

## 开发指南

### 项目结构
```
src/main/java/com/saltyfish/contract/gateway/
├── config/          # 配置类
├── controller/      # 控制器
├── dto/            # 数据传输对象
├── entity/         # 实体类
├── exception/      # 异常处理
├── filter/         # 过滤器
├── repository/     # 数据访问层
└── service/        # 业务服务层
```

### 扩展开发
1. **添加新的过滤器**: 实现 `GlobalFilter` 接口
2. **添加新的访问规则**: 扩展 `AccessControlService`
3. **添加新的监控指标**: 在 `MonitoringFilter` 中添加
4. **添加新的配置**: 扩展 `GatewayProperties`

## 故障排查

### 常见问题
1. **服务发现失败**: 检查Nacos连接配置
2. **数据库连接失败**: 检查PostgreSQL配置和网络
3. **Redis连接失败**: 检查Redis配置和网络
4. **JWT解析失败**: 检查JWT密钥配置

### 日志配置
```yaml
logging:
  level:
    com.saltyfish.contract.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

## 许可证

本项目采用 MIT 许可证。