package com.kite.libai.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.system.domain.SysMenu;
import com.kite.libai.system.domain.SysRole;
import com.kite.libai.system.domain.SysRoleMenu;
import com.kite.libai.system.domain.SysUserRole;
import com.kite.libai.system.domain.dto.RoleForm;
import com.kite.libai.system.domain.dto.RoleQuery;
import com.kite.libai.system.mapper.SysMenuMapper;
import com.kite.libai.system.mapper.SysRoleMapper;
import com.kite.libai.system.mapper.SysRoleMenuMapper;
import com.kite.libai.system.mapper.SysUserRoleMapper;
import com.kite.libai.system.service.ISysRoleService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 角色管理 Service 实现。
 *
 * @author kite
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysMenuMapper sysMenuMapper;

    @Autowired
    public SysRoleServiceImpl(SysRoleMenuMapper sysRoleMenuMapper,
                              SysUserRoleMapper sysUserRoleMapper,
                              SysMenuMapper sysMenuMapper) {
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
    }

    @Override
    public PageResult<SysRole> selectRolePage(RoleQuery query) {
        IPage<SysRole> page = page(query.toPage(), Wrappers.<SysRole>lambdaQuery()
                .like(StringUtils.hasText(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
                .like(StringUtils.hasText(query.getRoleKey()), SysRole::getRoleKey, query.getRoleKey())
                .eq(query.getStatus() != null, SysRole::getStatus, query.getStatus())
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId));
        return PageResult.of(page);
    }

    @Override
    public List<SysRole> selectEnabledRoles() {
        return list(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getStatus, SysConstants.STATUS_NORMAL)
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId));
    }

    @Override
    public SysRole selectRoleById(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw ServiceException.notFound("角色不存在或已被删除");
        }
        role.setMenuIds(sysMenuMapper.selectMenuIdsByRoleId(roleId));
        return role;
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertRole(RoleForm form) {
        checkRoleNameUnique(form.getRoleName(), null);
        checkRoleKeyUnique(form.getRoleKey(), null);

        SysRole role = new SysRole();
        copyFormToEntity(form, role);
        role.setRoleId(null);
        if (role.getStatus() == null) {
            role.setStatus(SysConstants.STATUS_NORMAL);
        }
        if (!StringUtils.hasText(role.getDataScope())) {
            role.setDataScope("1");
        }
        save(role);

        assignMenus(role.getRoleId(), form.getMenuIds());
        return role.getRoleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleForm form) {
        if (form.getRoleId() == null) {
            throw ServiceException.badRequest("角色ID不能为空");
        }
        SysRole current = selectRoleById(form.getRoleId());

        // 超级管理员的角色标识决定了全局放行逻辑,一旦被改掉会导致系统失去管理员
        if (SysConstants.SUPER_ADMIN_ROLE_KEY.equals(current.getRoleKey())
                && !SysConstants.SUPER_ADMIN_ROLE_KEY.equals(form.getRoleKey())) {
            throw ServiceException.badRequest("超级管理员的角色权限字符串不允许修改");
        }
        checkRoleNameUnique(form.getRoleName(), form.getRoleId());
        checkRoleKeyUnique(form.getRoleKey(), form.getRoleId());

        SysRole role = new SysRole();
        copyFormToEntity(form, role);
        role.setRoleId(form.getRoleId());
        updateById(role);

        assignMenus(form.getRoleId(), form.getMenuIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            throw ServiceException.badRequest("请选择要删除的角色");
        }
        for (Long roleId : roleIds) {
            SysRole role = selectRoleById(roleId);
            if (SysConstants.SUPER_ADMIN_ROLE_KEY.equals(role.getRoleKey())) {
                throw ServiceException.badRequest("超级管理员角色不允许删除");
            }
            long userCount = sysUserRoleMapper.selectCount(
                    Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, roleId));
            if (userCount > 0) {
                throw ServiceException.badRequest("角色「" + role.getRoleName() + "」已分配给用户,不允许删除");
            }
        }
        // 逻辑删除角色本身,同时物理清理菜单授权关系,避免残留脏关联
        removeByIds(roleIds);
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, roleIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(Long roleId, Integer status) {
        if (status == null
                || (!SysConstants.STATUS_NORMAL.equals(status) && !SysConstants.STATUS_DISABLED.equals(status))) {
            throw ServiceException.badRequest("状态取值只能是 0-正常 或 1-停用");
        }
        SysRole role = selectRoleById(roleId);
        if (SysConstants.SUPER_ADMIN_ROLE_KEY.equals(role.getRoleKey())
                && SysConstants.STATUS_DISABLED.equals(status)) {
            throw ServiceException.badRequest("超级管理员角色不允许停用");
        }

        SysRole update = new SysRole();
        update.setRoleId(roleId);
        update.setStatus(status);
        updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 覆盖语义:先清空原有授权,再写入新授权。menuIds 为 null 时视为不变更,
        // 为空集合时视为清空权限,两者语义不同,不能合并处理。
        if (menuIds == null) {
            return;
        }
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds.isEmpty()) {
            return;
        }

        // 去重,防止前端重复提交同一菜单导致联合主键冲突
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(menuIds));
        long existCount = sysMenuMapper.selectCount(
                Wrappers.<SysMenu>lambdaQuery().in(SysMenu::getMenuId, distinctIds));
        if (existCount != distinctIds.size()) {
            throw ServiceException.badRequest("授权的菜单中存在不存在或已删除的菜单");
        }

        // 授权量级最多几十条,逐条 insert 足够,不额外引入批量 SQL 增加维护成本
        for (Long menuId : distinctIds) {
            sysRoleMenuMapper.insert(new SysRoleMenu(roleId, menuId));
        }
    }

    private void checkRoleNameUnique(String roleName, Long excludeRoleId) {
        long count = count(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleName, roleName)
                .ne(excludeRoleId != null, SysRole::getRoleId, excludeRoleId));
        if (count > 0) {
            throw ServiceException.badRequest("角色名称「" + roleName + "」已存在");
        }
    }

    private void checkRoleKeyUnique(String roleKey, Long excludeRoleId) {
        long count = count(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleKey, roleKey)
                .ne(excludeRoleId != null, SysRole::getRoleId, excludeRoleId));
        if (count > 0) {
            throw ServiceException.badRequest("角色权限字符串「" + roleKey + "」已存在");
        }
    }

    private void copyFormToEntity(RoleForm form, SysRole role) {
        role.setRoleName(form.getRoleName());
        role.setRoleKey(form.getRoleKey());
        role.setRoleSort(form.getRoleSort());
        role.setDataScope(form.getDataScope());
        role.setStatus(form.getStatus());
        role.setRemark(form.getRemark());
    }
}
