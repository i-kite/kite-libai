package com.kite.libai.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * 用户角色关联实体,对应表 sys_user_role。
 *
 * <p>关联表采用物理删除,不带审计字段与逻辑删除标记,因此不继承 BaseEntity。
 * 表的真实主键是 (user_id, role_id) 联合主键,MyBatis-Plus 不支持联合主键,
 * 这里把 user_id 标为 INPUT 型主键仅为满足框架要求,业务上只使用
 * insert 与按条件 delete,不会调用 selectById / deleteById。
 *
 * @author kite
 */
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /** 角色ID */
    @TableField("role_id")
    private Long roleId;

    public SysUserRole() {
    }

    public SysUserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
