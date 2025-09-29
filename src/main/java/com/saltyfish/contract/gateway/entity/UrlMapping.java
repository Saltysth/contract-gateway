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
 * URL Mapping Entity
 * URL映射实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "url_mappings")
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 映射名称
     */
    @Column(name = "mapping_name", nullable = false, length = 100)
    private String mappingName;

    /**
     * 外部路径
     */
    @Column(name = "external_path", nullable = false, length = 500)
    private String externalPath;

    /**
     * 内部路径
     */
    @Column(name = "internal_path", nullable = false, length = 500)
    private String internalPath;

    /**
     * 目标服务名
     */
    @Column(name = "target_service", nullable = false, length = 100)
    private String targetService;

    /**
     * 映射类型：rewrite/redirect/alias
     */
    @Column(name = "mapping_type", nullable = false, length = 20)
    private String mappingType;

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