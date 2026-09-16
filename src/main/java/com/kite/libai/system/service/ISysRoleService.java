package com.kite.libai.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.system.domain.SysRole;
import com.kite.libai.system.domain.dto.RoleForm;
import com.kite.libai.system.domain.dto.RoleQuery;
import java.util.List;

/**
 * 角色管理 Service。
 *
 * @author kite
 */
public interface ISysRoleService extends IService<SysRole> {

    /**
     * 角色分页列表。
     *
     * @param query 查询条件
     */
    PageResult<SysRole> selectRolePage(RoleQuery query);

    /**
     * 查询全部可用角色,供用户分配角色时下拉选择。
     */
    List<SysRole> selectEnabledRoles();

    /**
     * 按ID查询角色详情,同时回填已授权的菜单ID集合。
     *
     * @param roleId 角色ID
     */
    SysRole selectRoleById(Long roleId);

    /**
     * 查询指定用户拥有的角色列表。
     *
     * @param userId 用户ID
     */
    List<SysRole> selectRolesByUserId(Long userId);

    /**
     * 新增角色,并按表单中的 menuIds 完成授权。
     *
     * @param form 角色表单
     * @return 新角色ID
     */
    Long insertRole(RoleForm form);

    /**
     * 修改角色,并按表单中的 menuIds 覆盖授权。
     *
     * @param form 角色表单
     */
    void updateRole(RoleForm form);

    /**
     * 批量删除角色。已分配给用户的角色拒绝删除,超级管理员角色不可删除。
     *
     * @param roleIds 角色ID集合
     */
    void deleteRoles(List<Long> roleIds);

    /**
     * 修改角色状态。
     *
     * @param roleId 角色ID
     * @param status 目标状态:0-正常 1-停用
     */
    void updateRoleStatus(Long roleId, Integer status);

    /**
     * 重新给角色授权菜单,采用"先清空再写入"的覆盖语义。
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID集合,传空表示清空全部权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);
}
