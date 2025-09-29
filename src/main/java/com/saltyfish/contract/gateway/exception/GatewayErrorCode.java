package com.saltyfish.contract.gateway.exception;

/**
 * Gateway Error Code
 * 网关错误码定义
 */
public enum GatewayErrorCode {

    // 访问控制相关错误
    ACCESS_DENIED("GATEWAY_001", "访问被拒绝"),
    IP_BLOCKED("GATEWAY_002", "IP地址被阻止"),
    USER_BLOCKED("GATEWAY_003", "用户被阻止"),
    API_BLOCKED("GATEWAY_004", "API被阻止"),

    // 认证相关错误
    TOKEN_INVALID("GATEWAY_101", "Token无效"),
    TOKEN_EXPIRED("GATEWAY_102", "Token已过期"),
    TOKEN_MISSING("GATEWAY_103", "缺少Token"),

    // 路由相关错误
    SERVICE_UNAVAILABLE("GATEWAY_201", "服务不可用"),
    ROUTE_NOT_FOUND("GATEWAY_202", "路由未找到"),
    LOAD_BALANCE_FAILED("GATEWAY_203", "负载均衡失败"),

    // 系统错误
    INTERNAL_ERROR("GATEWAY_500", "内部服务器错误"),
    CONFIG_ERROR("GATEWAY_501", "配置错误"),
    CACHE_ERROR("GATEWAY_502", "缓存错误");

    private final String code;
    private final String message;

    GatewayErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}