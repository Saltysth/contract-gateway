package com.saltyfish.contract.gateway.service;

import com.saltyfish.contract.gateway.entity.AccessRule;
import com.saltyfish.contract.gateway.repository.AccessRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Access Control Service
 * 访问控制服务，提供黑白名单检查功能（响应式版本）
 */
@Slf4j
@Service
public class AccessControlService {

    @Autowired
    private AccessRuleRepository accessRuleRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    private static final String CACHE_KEY_PREFIX = "gateway:access:rules:";
    private static final String CACHE_KEY_ALL_RULES = CACHE_KEY_PREFIX + "all";
    private static final Duration CACHE_EXPIRE = Duration.ofSeconds(300); // 5分钟缓存

    /**
     * 初始化访问规则缓存
     */
    public Mono<Void> initAccessRulesCache() {
        return refreshAccessRulesCache()
                .then()
                .doOnSubscribe(v -> log.info("开始初始化访问规则缓存"))
                .onErrorResume(e -> {
                    log.error("初始化访问规则缓存失败", e);
                    return Mono.empty();
                });
    }

    /**
     * 检查访问是否被允许
     *
     * @param path     请求路径
     * @param method   HTTP方法
     * @param clientIp 客户端IP
     * @param userId   用户ID（可选）
     * @return 是否允许访问
     */
    public Mono<Boolean> isAccessAllowed(String path, String method, String clientIp, String userId) {
        return getCachedAccessRules()
                .map(rules -> checkAccessRules(rules, path, method, clientIp, userId))
                .onErrorResume(e -> {
                    log.error("检查访问权限异常: path={}, method={}, clientIp={}", path, method, clientIp, e);
                    // 异常情况下拒绝访问（fail-closed安全策略）
                    return Mono.just(false);
                });
    }

    /**
     * 检查访问规则
     */
    private boolean checkAccessRules(List<AccessRule> rules, String path, String method, String clientIp, String userId) {
        // 按优先级检查规则
        for (AccessRule rule : rules) {
            if (!rule.getEnabled()) {
                continue;
            }

            boolean matched = matchRule(rule, path, method, clientIp, userId);
            if (matched) {
                if ("blacklist".equals(rule.getRuleType())) {
                    log.debug("命中黑名单规则: {}, path={}", rule.getRuleName(), path);
                    return false;
                } else if ("whitelist".equals(rule.getRuleType())) {
                    log.debug("命中白名单规则: {}, path={}", rule.getRuleName(), path);
                    return true;
                }
            }
        }

        // 如果没有匹配到任何规则，检查是否存在白名单规则
        boolean hasWhitelistRules = rules.stream()
                .anyMatch(rule -> rule.getEnabled() && "whitelist".equals(rule.getRuleType()));

        if (hasWhitelistRules) {
            // 存在白名单规则但未匹配，拒绝访问
            log.debug("存在白名单规则但未匹配，拒绝访问: path={}", path);
            return false;
        }

        // 默认允许访问
        return true;
    }

    /**
     * 匹配访问规则
     */
    private boolean matchRule(AccessRule rule, String path, String method, String clientIp, String userId) {
        String matchType = rule.getMatchType();
        String matchPattern = rule.getMatchPattern();
        String matchValue = rule.getMatchValue();

        switch (matchType) {
            case "path":
                return matchPath(path, matchPattern, matchValue);
            case "method":
                return matchMethod(method, matchPattern, matchValue);
            case "ip":
                return matchIp(clientIp, matchPattern, matchValue);
            case "user":
                return matchUser(userId, matchPattern, matchValue);
            default:
                log.warn("未知的匹配类型: {}", matchType);
                return false;
        }
    }

    /**
     * 匹配路径
     */
    private boolean matchPath(String path, String pattern, String value) {
        if (path == null || value == null) {
            return false;
        }

        switch (pattern) {
            case "exact":
                return path.equals(value);
            case "prefix":
                return path.startsWith(value);
            case "suffix":
                return path.endsWith(value);
            case "wildcard":
                return matchWildcard(path, value);
            case "regex":
                return matchRegex(path, value);
            default:
                return false;
        }
    }

    /**
     * 匹配HTTP方法
     */
    private boolean matchMethod(String method, String pattern, String value) {
        if (method == null || value == null) {
            return false;
        }
        return method.equalsIgnoreCase(value);
    }

    /**
     * 匹配IP地址
     */
    private boolean matchIp(String clientIp, String pattern, String value) {
        if (clientIp == null || value == null) {
            return false;
        }

        switch (pattern) {
            case "exact":
                return clientIp.equals(value);
            case "prefix":
                return clientIp.startsWith(value);
            case "cidr":
                return matchCidr(clientIp, value);
            case "regex":
                return matchRegex(clientIp, value);
            default:
                return false;
        }
    }

    /**
     * 匹配用户
     */
    private boolean matchUser(String userId, String pattern, String value) {
        if (userId == null || value == null) {
            return false;
        }
        return userId.equals(value);
    }

    /**
     * 通配符匹配
     */
    private boolean matchWildcard(String text, String pattern) {
        try {
            String regex = pattern.replace("*", ".*").replace("?", ".");
            return Pattern.matches(regex, text);
        } catch (Exception e) {
            log.warn("通配符匹配异常: text={}, pattern={}", text, pattern, e);
            return false;
        }
    }

    /**
     * 正则表达式匹配
     */
    private boolean matchRegex(String text, String regex) {
        try {
            return Pattern.matches(regex, text);
        } catch (Exception e) {
            log.warn("正则表达式匹配异常: text={}, regex={}", text, regex, e);
            return false;
        }
    }

    /**
     * CIDR匹配
     * TODO: 实现CIDR网段匹配逻辑
     */
    private boolean matchCidr(String ip, String cidr) {
        // 简单实现，实际应该使用专门的CIDR匹配库
        log.warn("CIDR匹配暂未实现: ip={}, cidr={}", ip, cidr);
        return false;
    }

    /**
     * 获取缓存的访问规则
     */
    @SuppressWarnings("unchecked")
    private Mono<List<AccessRule>> getCachedAccessRules() {
        return reactiveRedisTemplate.opsForValue().get(CACHE_KEY_ALL_RULES)
                .cast(List.class)
                .map(list -> (List<AccessRule>) list)
                .switchIfEmpty(Mono.defer(() -> refreshAccessRulesCache()));
    }

    /**
     * 刷新访问规则缓存
     */
    public Mono<List<AccessRule>> refreshAccessRulesCache() {
        return accessRuleRepository.findEnabledRulesOrderByPriority()
                .collectList()
                .flatMap(rules -> reactiveRedisTemplate.opsForValue()
                        .set(CACHE_KEY_ALL_RULES, rules, CACHE_EXPIRE)
                        .thenReturn(rules))
                .doOnNext(rules -> log.info("访问规则缓存已刷新，规则数量: {}", rules.size()))
                .onErrorResume(e -> {
                    log.error("刷新访问规则缓存失败", e);
                    return Mono.just(List.of());
                });
    }

    /**
     * 清除访问规则缓存
     */
    public Mono<Void> clearAccessRulesCache() {
        return reactiveRedisTemplate.delete(CACHE_KEY_ALL_RULES)
                .doOnSuccess(v -> log.info("访问规则缓存已清除"))
                .onErrorResume(e -> {
                    log.error("清除访问规则缓存失败", e);
                    return Mono.empty();
                })
                .then();
    }
}
