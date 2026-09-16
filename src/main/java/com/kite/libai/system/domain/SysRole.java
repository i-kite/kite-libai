package com.kite.libai.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kite.libai.common.core.domain.BaseEntity;
import java.util.List;

/**
 * 角色实体,对应表 sys_role。
 *
 * @author kite
 */
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /** 角色名称 */
    @TableField("role_name")
    private String roleName;

    /** 角色权限字符串,代码中按此判断是否超级管理员 */
    @TableField("role_key")
    private String roleKey;

    /** 显示顺序 */
    @TableField("role_sort")
    private Integer roleSort;

    /** 数据范围:1-全部 2-自定义 3-本部门 4-本部门及以下 5-仅本人 */
    @TableField("data_scope")
    private String dataScope;

    /** 角色状态:0-正常 1-停用 */
    @TableField("status")
    private Integer status;

    /** 角色关联的菜单ID集合,非数据库字段,查询详情与授权时使用 */
    @TableField(exist = false)
    private List<Long> menuIds;

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
}
