package com.saltyfish.contract.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Global Exception Handler
 * 全局异常处理器
 */
@Slf4j
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 设置响应头
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 根据异常类型设置状态码和错误信息
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = GatewayErrorCode.INTERNAL_ERROR.getCode();
        String errorMessage = GatewayErrorCode.INTERNAL_ERROR.getMessage();

        if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = "GATEWAY_400";
            errorMessage = "请求参数错误";
        } else if (ex instanceof SecurityException) {
            status = HttpStatus.FORBIDDEN;
            errorCode = GatewayErrorCode.ACCESS_DENIED.getCode();
            errorMessage = GatewayErrorCode.ACCESS_DENIED.getMessage();
        }

        response.setStatusCode(status);

        // 构建错误响应
        String errorResponse = String.format(
                "{\"code\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}",
                errorCode, errorMessage, System.currentTimeMillis()
        );

        log.error("网关异常处理: path={}, error={}", 
                 exchange.getRequest().getURI().getPath(), ex.getMessage(), ex);

        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}