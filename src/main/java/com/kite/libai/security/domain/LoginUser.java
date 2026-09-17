package com.kite.libai.security.domain;

import com.kite.libai.common.constant.SysConstants;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 当前登录用户。
 *
 * @author kite
 */
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userName;

    private String nickName;

    private Long deptId;

    /** 角色权限字符串集合 */
    private List<String> roles;

    /** 权限标识集合,超级管理员为通配符 */
    private Set<String> permissions;

    public LoginUser() {
        this.roles = Collections.emptyList();
        this.permissions = Collections.emptySet();
    }

    /**
     * 是否超级管理员。
     */
    public boolean isAdmin() {
        return roles != null && roles.contains(SysConstants.SUPER_ADMIN_ROLE_KEY);
    }

    /**
     * 是否拥有指定权限标识。超级管理员或持有通配符权限时直接放行。
     *
     * @param permission 权限标识,例如 system:user:add
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        if (isAdmin() || (permissions != null && permissions.contains(SysConstants.ALL_PERMISSION))) {
            return true;
        }
        return permissions != null && permissions.contains(permission);
    }

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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles == null ? Collections.emptyList() : roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions == null ? Collections.emptySet() : permissions;
    }
}
