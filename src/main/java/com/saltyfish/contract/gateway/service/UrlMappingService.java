package com.saltyfish.contract.gateway.service;

import com.saltyfish.contract.gateway.entity.UrlMapping;
import com.saltyfish.contract.gateway.repository.UrlMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * URL Mapping Service
 * URL映射服务，提供路径重写和服务路由功能（响应式版本）
 */
@Slf4j
@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    private static final String CACHE_KEY_PREFIX = "gateway:url:mappings:";
    private static final String CACHE_KEY_ALL_MAPPINGS = CACHE_KEY_PREFIX + "all";
    private static final Duration CACHE_EXPIRE = Duration.ofSeconds(300); // 5分钟缓存

    /**
     * 初始化URL映射缓存
     */
    public Mono<Void> initUrlMappingsCache() {
        return refreshUrlMappingsCache()
                .then()
                .doOnSubscribe(v -> log.info("开始初始化URL映射缓存"))
                .onErrorResume(e -> {
                    log.error("初始化URL映射缓存失败", e);
                    return Mono.empty();
                });
    }

    /**
     * 根据外部路径查找URL映射
     *
     * @param externalPath 外部路径
     * @return URL映射信息
     */
    public Mono<UrlMapping> findMapping(String externalPath) {
        return getCachedUrlMappings()
                .flatMapIterable(mappings -> mappings)
                .filter(mapping -> mapping.getEnabled() && matchPath(externalPath, mapping.getExternalPath()))
                .next()
                .doOnNext(mapping -> log.debug("找到URL映射: {} -> {}", externalPath, mapping.getInternalPath()))
                .doOnTerminate(() -> log.debug("未找到URL映射: {}", externalPath))
                .onErrorResume(e -> {
                    log.error("查找URL映射异常: externalPath={}", externalPath, e);
                    return Mono.empty();
                });
    }

    /**
     * 重写路径
     *
     * @param originalPath 原始路径
     * @param mapping      URL映射
     * @return 重写后的路径
     */
    public String rewritePath(String originalPath, UrlMapping mapping) {
        if (mapping == null) {
            return originalPath;
        }

        try {
            String externalPath = mapping.getExternalPath();
            String internalPath = mapping.getInternalPath();

            // 处理通配符映射
            if (externalPath.endsWith("/**")) {
                String prefix = externalPath.substring(0, externalPath.length() - 3);
                if (originalPath.startsWith(prefix)) {
                    String suffix = originalPath.substring(prefix.length());
                    String targetPrefix = internalPath.endsWith("/**") ?
                                        internalPath.substring(0, internalPath.length() - 3) : internalPath;
                    return targetPrefix + suffix;
                }
            }

            // 精确匹配
            if (originalPath.equals(externalPath)) {
                return internalPath;
            }

            // 前缀匹配
            if (originalPath.startsWith(externalPath)) {
                String suffix = originalPath.substring(externalPath.length());
                return internalPath + suffix;
            }

            return originalPath;

        } catch (Exception e) {
            log.error("路径重写异常: originalPath={}, mapping={}", originalPath, mapping.getMappingName(), e);
            return originalPath;
        }
    }

    /**
     * 匹配路径
     */
    private boolean matchPath(String path, String pattern) {
        if (path == null || pattern == null) {
            return false;
        }

        // 精确匹配
        if (path.equals(pattern)) {
            return true;
        }

        // 通配符匹配
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }

        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
        }

        // 正则匹配
        if (pattern.startsWith("regex:")) {
            String regex = pattern.substring(6);
            try {
                return Pattern.matches(regex, path);
            } catch (Exception e) {
                log.warn("正则匹配异常: path={}, regex={}", path, regex, e);
                return false;
            }
        }

        return false;
    }

    /**
     * 获取缓存的URL映射
     */
    @SuppressWarnings("unchecked")
    private Mono<List<UrlMapping>> getCachedUrlMappings() {
        return reactiveRedisTemplate.opsForValue().get(CACHE_KEY_ALL_MAPPINGS)
                .cast(List.class)
                .map(list -> (List<UrlMapping>) list)
                .switchIfEmpty(Mono.defer(() -> refreshUrlMappingsCache()));
    }

    /**
     * 刷新URL映射缓存
     */
    public Mono<List<UrlMapping>> refreshUrlMappingsCache() {
        return urlMappingRepository.findEnabledMappingsOrderByPriority()
                .collectList()
                .flatMap(mappings -> reactiveRedisTemplate.opsForValue()
                        .set(CACHE_KEY_ALL_MAPPINGS, mappings, CACHE_EXPIRE)
                        .thenReturn(mappings))
                .doOnNext(mappings -> log.info("URL映射缓存已刷新，映射数量: {}", mappings.size()))
                .onErrorResume(e -> {
                    log.error("刷新URL映射缓存失败", e);
                    return Mono.just(List.of());
                });
    }

    /**
     * 清除URL映射缓存
     */
    public Mono<Void> clearUrlMappingsCache() {
        return reactiveRedisTemplate.delete(CACHE_KEY_ALL_MAPPINGS)
                .doOnSuccess(v -> log.info("URL映射缓存已清除"))
                .onErrorResume(e -> {
                    log.error("清除URL映射缓存失败", e);
                    return Mono.empty();
                })
                .then();
    }
}
