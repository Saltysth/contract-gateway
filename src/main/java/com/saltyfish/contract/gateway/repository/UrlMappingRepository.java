package com.saltyfish.contract.gateway.repository;

import com.saltyfish.contract.gateway.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * URL Mapping Repository
 * URL映射数据访问层
 */
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * 查询启用的URL映射，按优先级降序排列
     */
    @Query("SELECT um FROM UrlMapping um WHERE um.enabled = true ORDER BY um.priority DESC, um.id ASC")
    List<UrlMapping> findEnabledMappingsOrderByPriority();

    /**
     * 根据外部路径查询URL映射
     */
    @Query("SELECT um FROM UrlMapping um WHERE um.enabled = true AND um.externalPath = :externalPath")
    Optional<UrlMapping> findEnabledMappingByExternalPath(@Param("externalPath") String externalPath);

    /**
     * 根据目标服务查询URL映射
     */
    @Query("SELECT um FROM UrlMapping um WHERE um.enabled = true AND um.targetService = :targetService ORDER BY um.priority DESC, um.id ASC")
    List<UrlMapping> findEnabledMappingsByTargetService(@Param("targetService") String targetService);
}