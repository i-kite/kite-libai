package com.kite.libai.system.domain.dto;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 角色新增/修改入参。
 *
 * <p>{@code menuIds} 同时承担授权功能:新增或修改角色时一并覆盖其菜单权限。
 *
 * @author kite
 */
public class RoleForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色ID,新增时为空,修改时必填 */
    private Long roleId;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    @NotBlank(message = "角色权限字符串不能为空")
    @Size(max = 100, message = "角色权限字符串长度不能超过100个字符")
    private String roleKey;

    @NotNull(message = "显示顺序不能为空")
    private Integer roleSort;

    /** 数据范围:1-全部 2-自定义 3-本部门 4-本部门及以下 5-仅本人 */
    private String dataScope;

    /** 角色状态:0-正常 1-停用 */
    private Integer status;

    /** 授予该角色的菜单ID集合,传空集合表示清空权限 */
    private List<Long> menuIds;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

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

    public Integer getRoleSort() {
        return roleSort;
    }

    public void setRoleSort(Integer roleSort) {
        this.roleSort = roleSort;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
