package com.saltyfish.contract.gateway.repository;

import com.saltyfish.contract.gateway.entity.AccessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Access Rule Repository
 * 访问规则数据访问层
 */
@Repository
public interface AccessRuleRepository extends JpaRepository<AccessRule, Long> {

    /**
     * 查询启用的访问规则，按优先级降序排列
     */
    @Query("SELECT ar FROM AccessRule ar WHERE ar.enabled = true ORDER BY ar.priority DESC, ar.id ASC")
    List<AccessRule> findEnabledRulesOrderByPriority();

    /**
     * 根据规则类型查询启用的访问规则
     */
    @Query("SELECT ar FROM AccessRule ar WHERE ar.enabled = true AND ar.ruleType = :ruleType ORDER BY ar.priority DESC, ar.id ASC")
    List<AccessRule> findEnabledRulesByType(@Param("ruleType") String ruleType);

    /**
     * 根据匹配类型查询启用的访问规则
     */
    @Query("SELECT ar FROM AccessRule ar WHERE ar.enabled = true AND ar.matchType = :matchType ORDER BY ar.priority DESC, ar.id ASC")
    List<AccessRule> findEnabledRulesByMatchType(@Param("matchType") String matchType);
}