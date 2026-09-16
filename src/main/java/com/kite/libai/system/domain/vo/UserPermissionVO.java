package com.kite.libai.system.domain.vo;

import com.kite.libai.system.domain.SysMenu;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户权限信息,登录后由前端一次性拉取,用于渲染菜单与控制按钮显隐。
 *
 * @author kite
 */
public class UserPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 登录账号 */
    private String userName;

    /** 是否超级管理员,前端可据此跳过权限判断 */
    private boolean admin;

    /** 角色权限字符串集合,例如 ["admin"] */
    private List<String> roles;

    /** 权限标识集合,超级管理员为 ["*:*:*"] */
    private Set<String> permissions;

    /** 有权访问的菜单树(只含目录与菜单,不含按钮) */
    private List<SysMenu> menus;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public List<SysMenu> getMenus() {
        return menus;
    }

    public void setMenus(List<SysMenu> menus) {
        this.menus = menus;
    }
}
