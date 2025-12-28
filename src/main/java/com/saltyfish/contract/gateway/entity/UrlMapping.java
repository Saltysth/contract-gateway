package com.saltyfish.contract.gateway.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * URL Mapping Entity
 * URL映射实体类（WebFlux响应式版本）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "url_mappings")
public class UrlMapping implements Persistable<Long> {

    @Id
    private Long id;

    /**
     * 映射名称
     */
    @Column("mapping_name")
    private String mappingName;

    /**
     * 外部路径
     */
    @Column("external_path")
    private String externalPath;

    /**
     * 内部路径
     */
    @Column("internal_path")
    private String internalPath;

    /**
     * 目标服务名
     */
    @Column("target_service")
    private String targetService;

    /**
     * 映射类型：rewrite/redirect/alias
     */
    @Column("mapping_type")
    private String mappingType;

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