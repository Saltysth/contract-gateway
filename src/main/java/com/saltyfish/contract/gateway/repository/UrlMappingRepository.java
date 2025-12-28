package com.saltyfish.contract.gateway.repository;

import com.saltyfish.contract.gateway.entity.UrlMapping;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * URL Mapping Repository
 * URL映射数据访问层（WebFlux响应式版本）
 */
@Repository
public interface UrlMappingRepository extends R2dbcRepository<UrlMapping, Long> {

    /**
     * 查询启用的URL映射，按优先级降序排列
     */
    @Query("SELECT * FROM url_mappings WHERE enabled = true ORDER BY priority DESC, id ASC")
    Flux<UrlMapping> findEnabledMappingsOrderByPriority();

    /**
     * 根据外部路径查询URL映射
     */
    @Query("SELECT * FROM url_mappings WHERE enabled = true AND external_path = :externalPath LIMIT 1")
    Mono<UrlMapping> findEnabledMappingByExternalPath(@Param("externalPath") String externalPath);

    /**
     * 根据目标服务查询URL映射
     */
    @Query("SELECT * FROM url_mappings WHERE enabled = true AND target_service = :targetService ORDER BY priority DESC, id ASC")
    Flux<UrlMapping> findEnabledMappingsByTargetService(@Param("targetService") String targetService);
}
