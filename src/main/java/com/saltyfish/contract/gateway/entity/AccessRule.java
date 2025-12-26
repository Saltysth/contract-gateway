package com.saltyfish.contract.gateway.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Access Rule Entity
 * 访问规则实体类（WebFlux响应式版本）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "access_rules")
public class AccessRule implements Persistable<Long> {

    @Id
    private Long id;

    /**
     * 规则名称
     */
    @Column("rule_name")
    private String ruleName;

    /**
     * 规则类型：whitelist/blacklist
     */
    @Column("rule_type")
    private String ruleType;

    /**
     * 匹配类型：path/ip/user/method
     */
    @Column("match_type")
    private String matchType;

    /**
     * 匹配模式：exact/prefix/wildcard/regex
     */
    @Column("match_pattern")
    private String matchPattern;

    /**
     * 匹配值
     */
    @Column("match_value")
    private String matchValue;

    /**
     * 优先级，数值越大优先级越高
     */
    @Column("priority")
    private Integer priority = 0;

    /**
     * 是否启用
     */
    @Column("enabled")
    private Boolean enabled = true;

    /**
     * 描述
     */
    @Column("description")
    private String description;

    /**
     * 创建时间
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean isNew() {
        return id == null;
    }

    /**
     * 创建时设置时间
     */
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    /**
     * 更新时设置时间
     */
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}