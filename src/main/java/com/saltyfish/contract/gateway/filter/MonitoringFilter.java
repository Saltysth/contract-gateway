package com.saltyfish.contract.gateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Monitoring Filter
 * 监控过滤器，收集请求指标数据
 */
@Slf4j
@Component
public class MonitoringFilter implements GlobalFilter, Ordered {

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> recordMetrics(request, exchange.getResponse(), startTime, null))
                .doOnError(throwable -> recordMetrics(request, exchange.getResponse(), startTime, throwable));
    }

    /**
     * 记录监控指标
     */
    private void recordMetrics(ServerHttpRequest request, ServerHttpResponse response, long startTime, Throwable error) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            String path = request.getURI().getPath();
            String method = request.getMethod().name();
            String statusCode = response.getStatusCode() != null ? 
                              response.getStatusCode().toString() : "unknown";

            // 记录请求计数
            Counter.builder("gateway.requests.total")
                    .tag("path", path)
                    .tag("method", method)
                    .tag("status", statusCode)
                    .register(meterRegistry)
                    .increment();

            // 记录响应时间
            Timer.builder("gateway.requests.duration")
                    .tag("path", path)
                    .tag("method", method)
                    .tag("status", statusCode)
                    .register(meterRegistry)
                    .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

            // 记录错误计数
            if (error != null) {
                Counter.builder("gateway.requests.errors")
                        .tag("path", path)
                        .tag("method", method)
                        .tag("error", error.getClass().getSimpleName())
                        .register(meterRegistry)
                        .increment();
            }

            log.debug("监控指标已记录: path={}, method={}, status={}, duration={}ms", 
                     path, method, statusCode, duration);

        } catch (Exception e) {
            log.error("记录监控指标异常", e);
        }
    }

    @Override
    public int getOrder() {
        // 最后执行，记录完整的请求处理过程
        return Ordered.LOWEST_PRECEDENCE;
    }
}