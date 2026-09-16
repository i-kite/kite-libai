package com.kite.libai.system.service.impl;

import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.util.TreeUtils;
import com.kite.libai.system.domain.SysMenu;
import com.kite.libai.system.domain.SysRole;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.vo.UserPermissionVO;
import com.kite.libai.system.mapper.SysMenuMapper;
import com.kite.libai.system.service.ISysMenuService;
import com.kite.libai.system.service.ISysPermissionService;
import com.kite.libai.system.service.ISysRoleService;
import com.kite.libai.system.service.ISysUserService;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 权限聚合查询 Service 实现。
 *
 * @author kite
 */
@Service
public class SysPermissionServiceImpl implements ISysPermissionService {

    private final ISysUserService sysUserService;

    private final ISysRoleService sysRoleService;

    private final ISysMenuService sysMenuService;

    private final SysMenuMapper sysMenuMapper;

    @Autowired
    public SysPermissionServiceImpl(ISysUserService sysUserService,
                                    ISysRoleService sysRoleService,
                                    ISysMenuService sysMenuService,
                                    SysMenuMapper sysMenuMapper) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.sysMenuService = sysMenuService;
        this.sysMenuMapper = sysMenuMapper;
    }

    @Override
    public UserPermissionVO getUserPermission(Long userId) {
        SysUser user = sysUserService.selectUserById(userId);
        List<SysRole> roles = sysRoleService.selectRolesByUserId(userId);

        List<String> roleKeys = roles.stream()
                .filter(role -> SysConstants.STATUS_NORMAL.equals(role.getStatus()))
                .map(SysRole::getRoleKey)
                .collect(Collectors.toList());
        boolean admin = roleKeys.contains(SysConstants.SUPER_ADMIN_ROLE_KEY);

        UserPermissionVO vo = new UserPermissionVO();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setAdmin(admin);
        vo.setRoles(roleKeys);

        if (admin) {
            // 超级管理员走通配符,不再逐条查关联表,同时保证新增菜单后无需重新授权
            vo.setPermissions(Collections.singleton(SysConstants.ALL_PERMISSION));
            vo.setMenus(buildAdminMenuTree());
        } else {
            Set<String> perms = sysMenuMapper.selectPermsByUserId(userId).stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            vo.setPermissions(perms);
            vo.setMenus(buildMenuTree(sysMenuMapper.selectMenusByUserId(userId)));
        }
        return vo;
    }

    /**
     * 超级管理员的菜单树:全部启用且可见的目录与菜单。
     */
    private List<SysMenu> buildAdminMenuTree() {
        List<SysMenu> all = sysMenuService.selectMenuList(null, SysConstants.STATUS_NORMAL).stream()
                .filter(menu -> !SysConstants.MENU_TYPE_BUTTON.equals(menu.getMenuType()))
                .filter(menu -> menu.getVisible() == null || menu.getVisible() == 0)
                .collect(Collectors.toList());
        return buildMenuTree(all);
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        return TreeUtils.build(menus, SysMenu::getMenuId, SysMenu::getParentId, SysMenu::setChildren);
    }
}
