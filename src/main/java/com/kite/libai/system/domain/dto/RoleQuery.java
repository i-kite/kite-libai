package com.kite.libai.system.domain.dto;

import com.kite.libai.common.core.page.PageQuery;

/**
 * 角色分页查询条件。
 *
 * @author kite
 */
public class RoleQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    /** 角色名称,模糊匹配 */
    private String roleName;

    /** 角色权限字符串,模糊匹配 */
    private String roleKey;

    /** 角色状态:0-正常 1-停用 */
    private Integer status;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
