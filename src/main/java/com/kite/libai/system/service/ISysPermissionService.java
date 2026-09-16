package com.kite.libai.system.service;

import com.kite.libai.system.domain.vo.UserPermissionVO;

/**
 * 权限聚合查询 Service。
 *
 * <p>把"用户 -> 角色 -> 菜单/权限标识"的完整链路收口到一个接口,
 * 前端登录后一次性拉取,避免多次往返。
 *
 * @author kite
 */
public interface ISysPermissionService {

    /**
     * 查询用户的角色、权限标识与菜单树。
     *
     * <p>超级管理员(拥有 role_key = admin 的角色)直接返回权限通配符与全部菜单,
     * 不再逐条查询关联表。
     *
     * @param userId 用户ID
     */
    UserPermissionVO getUserPermission(Long userId);
}
