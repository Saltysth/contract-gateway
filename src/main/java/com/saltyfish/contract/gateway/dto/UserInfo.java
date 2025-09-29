package com.saltyfish.contract.gateway.dto;

import lombok.Data;

import java.util.List;

/**
 * User Information DTO
 * 用户信息传输对象
 */
@Data
public class UserInfo {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 扩展属性
     */
    private Object extensions;
}