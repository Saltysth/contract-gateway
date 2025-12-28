package com.saltyfish.contract.gateway.repository;

import com.saltyfish.contract.gateway.entity.AccessRule;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Access Rule Repository
 * 访问规则数据访问层（WebFlux响应式版本）
 */
@Repository
public interface AccessRuleRepository extends R2dbcRepository<AccessRule, Long> {

    /**
     * 查询启用的访问规则，按优先级降序排列
     */
    @Query("SELECT * FROM access_rules WHERE enabled = true ORDER BY priority DESC, id ASC")
    Flux<AccessRule> findEnabledRulesOrderByPriority();

    /**
     * 根据规则类型查询启用的访问规则
     */
    @Query("SELECT * FROM access_rules WHERE enabled = true AND rule_type = :ruleType ORDER BY priority DESC, id ASC")
    Flux<AccessRule> findEnabledRulesByType(@Param("ruleType") String ruleType);

    /**
     * 根据匹配类型查询启用的访问规则
     */
    @Query("SELECT * FROM access_rules WHERE enabled = true AND match_type = :matchType ORDER BY priority DESC, id ASC")
    Flux<AccessRule> findEnabledRulesByMatchType(@Param("matchType") String matchType);
}
