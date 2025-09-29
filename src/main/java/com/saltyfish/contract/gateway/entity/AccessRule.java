package com.saltyfish.contract.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Access Rule Entity
 * 访问规则实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "access_rules")
public class AccessRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    /**
     * 规则类型：whitelist/blacklist
     */
    @Column(name = "rule_type", nullable = false, length = 20)
    private String ruleType;

    /**
     * 匹配类型：path/ip/user/method
     */
    @Column(name = "match_type", nullable = false, length = 20)
    private String matchType;

    /**
     * 匹配模式：exact/prefix/wildcard/regex
     */
    @Column(name = "match_pattern", nullable = false, length = 20)
    private String matchPattern;

    /**
     * 匹配值
     */
    @Column(name = "match_value", nullable = false, length = 500)
    private String matchValue;

    /**
     * 优先级，数值越大优先级越高
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}