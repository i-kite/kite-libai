package com.kite.libai.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.common.util.TreeUtils;
import com.kite.libai.system.domain.SysMenu;
import com.kite.libai.system.domain.SysRoleMenu;
import com.kite.libai.system.domain.dto.MenuForm;
import com.kite.libai.system.mapper.SysMenuMapper;
import com.kite.libai.system.mapper.SysRoleMenuMapper;
import com.kite.libai.system.service.ISysMenuService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 菜单权限管理 Service 实现。
 *
 * @author kite
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    public SysMenuServiceImpl(SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    @Override
    public List<SysMenu> selectMenuList(String menuName, Integer status) {
        return list(Wrappers.<SysMenu>lambdaQuery()
                .like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName)
                .eq(status != null, SysMenu::getStatus, status)
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum));
    }

    @Override
    public List<SysMenu> selectMenuTree(String menuName, Integer status) {
        List<SysMenu> flatList = selectMenuList(menuName, status);
        return TreeUtils.build(flatList, SysMenu::getMenuId, SysMenu::getParentId, SysMenu::setChildren);
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        SysMenu menu = getById(menuId);
        if (menu == null) {
            throw ServiceException.notFound("菜单不存在或已被删除");
        }
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertMenu(MenuForm form) {
        checkParentExists(form.getParentId());
        checkMenuNameUnique(form.getParentId(), form.getMenuName(), null);
        checkPermsUnique(form.getPerms(), null);

        SysMenu menu = new SysMenu();
        copyFormToEntity(form, menu);
        menu.setMenuId(null);
        applyDefaults(menu);
        save(menu);
        return menu.getMenuId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuForm form) {
        if (form.getMenuId() == null) {
            throw ServiceException.badRequest("菜单ID不能为空");
        }
        selectMenuById(form.getMenuId());

        if (form.getMenuId().equals(form.getParentId())) {
            throw ServiceException.badRequest("上级菜单不能是自己");
        }
        checkParentExists(form.getParentId());
        checkMenuNameUnique(form.getParentId(), form.getMenuName(), form.getMenuId());
        checkPermsUnique(form.getPerms(), form.getMenuId());

        SysMenu menu = new SysMenu();
        copyFormToEntity(form, menu);
        menu.setMenuId(form.getMenuId());
        updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        selectMenuById(menuId);

        long childCount = count(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, menuId));
        if (childCount > 0) {
            throw ServiceException.badRequest("该菜单下仍存在子菜单,不允许删除");
        }

        long assignedCount = sysRoleMenuMapper.selectCount(
                Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, menuId));
        if (assignedCount > 0) {
            throw ServiceException.badRequest("该菜单已被角色引用,请先取消授权后再删除");
        }

        removeById(menuId);
    }

    /**
     * 按钮类型的菜单不需要路由,目录与菜单必须有路由地址。
     */
    private void applyDefaults(SysMenu menu) {
        if (menu.getStatus() == null) {
            menu.setStatus(SysConstants.STATUS_NORMAL);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(0);
        }
        if (menu.getIsFrame() == null) {
            menu.setIsFrame(1);
        }
    }

    private void checkParentExists(Long parentId) {
        if (SysConstants.ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        if (getById(parentId) == null) {
            throw ServiceException.badRequest("上级菜单不存在或已被删除");
        }
    }

    private void checkMenuNameUnique(Long parentId, String menuName, Long excludeMenuId) {
        long count = count(Wrappers.<SysMenu>lambdaQuery()
                .eq(SysMenu::getParentId, parentId)
                .eq(SysMenu::getMenuName, menuName)
                .ne(excludeMenuId != null, SysMenu::getMenuId, excludeMenuId));
        if (count > 0) {
            throw ServiceException.badRequest("同级菜单下已存在名称为「" + menuName + "」的菜单");
        }
    }

    /**
     * 权限标识全局唯一。空标识(目录通常没有)不参与校验。
     */
    private void checkPermsUnique(String perms, Long excludeMenuId) {
        if (!StringUtils.hasText(perms)) {
            return;
        }
        long count = count(Wrappers.<SysMenu>lambdaQuery()
                .eq(SysMenu::getPerms, perms)
                .ne(excludeMenuId != null, SysMenu::getMenuId, excludeMenuId));
        if (count > 0) {
            throw ServiceException.badRequest("权限标识「" + perms + "」已存在");
        }
    }

    private void copyFormToEntity(MenuForm form, SysMenu menu) {
        menu.setMenuName(form.getMenuName());
        menu.setParentId(form.getParentId());
        menu.setOrderNum(form.getOrderNum());
        menu.setPath(form.getPath());
        menu.setComponent(form.getComponent());
        menu.setIsFrame(form.getIsFrame());
        menu.setMenuType(form.getMenuType());
        menu.setVisible(form.getVisible());
        menu.setStatus(form.getStatus());
        menu.setPerms(form.getPerms());
        menu.setIcon(form.getIcon());
        menu.setRemark(form.getRemark());
    }
}
