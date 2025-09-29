package com.saltyfish.contract.gateway.service;

import com.saltyfish.contract.gateway.entity.UrlMapping;
import com.saltyfish.contract.gateway.repository.UrlMappingRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * URL Mapping Service
 * URL映射服务，提供路径重写和服务路由功能
 */
@Slf4j
@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "gateway:url:mappings:";
    private static final String CACHE_KEY_ALL_MAPPINGS = CACHE_KEY_PREFIX + "all";
    private static final long CACHE_EXPIRE_SECONDS = 300; // 5分钟缓存

    /**
     * 初始化URL映射缓存
     */
    @PostConstruct
    public void initUrlMappingsCache() {
        refreshUrlMappingsCache();
    }

    /**
     * 根据外部路径查找URL映射
     *
     * @param externalPath 外部路径
     * @return URL映射信息
     */
    public UrlMapping findMapping(String externalPath) {
        try {
            List<UrlMapping> mappings = getCachedUrlMappings();
            
            // 按优先级查找匹配的映射
            for (UrlMapping mapping : mappings) {
                if (!mapping.getEnabled()) {
                    continue;
                }
                
                if (matchPath(externalPath, mapping.getExternalPath())) {
                    log.debug("找到URL映射: {} -> {}", externalPath, mapping.getInternalPath());
                    return mapping;
                }
            }
            
            log.debug("未找到URL映射: {}", externalPath);
            return null;
            
        } catch (Exception e) {
            log.error("查找URL映射异常: externalPath={}", externalPath, e);
            return null;
        }
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
    private List<UrlMapping> getCachedUrlMappings() {
        try {
            List<UrlMapping> mappings = (List<UrlMapping>) redisTemplate.opsForValue().get(CACHE_KEY_ALL_MAPPINGS);
            if (mappings == null) {
                mappings = refreshUrlMappingsCache();
            }
            return mappings;
        } catch (Exception e) {
            log.error("获取缓存URL映射失败，从数据库加载", e);
            return urlMappingRepository.findEnabledMappingsOrderByPriority();
        }
    }

    /**
     * 刷新URL映射缓存
     */
    public List<UrlMapping> refreshUrlMappingsCache() {
        try {
            List<UrlMapping> mappings = urlMappingRepository.findEnabledMappingsOrderByPriority();
            redisTemplate.opsForValue().set(CACHE_KEY_ALL_MAPPINGS, mappings, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.info("URL映射缓存已刷新，映射数量: {}", mappings.size());
            return mappings;
        } catch (Exception e) {
            log.error("刷新URL映射缓存失败", e);
            return List.of();
        }
    }

    /**
     * 清除URL映射缓存
     */
    public void clearUrlMappingsCache() {
        try {
            redisTemplate.delete(CACHE_KEY_ALL_MAPPINGS);
            log.info("URL映射缓存已清除");
        } catch (Exception e) {
            log.error("清除URL映射缓存失败", e);
        }
    }
}