package com.kite.libai.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kite.libai.AbstractIntegrationTest;
import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.system.domain.SysRole;
import com.kite.libai.system.domain.dto.RoleForm;
import com.kite.libai.system.domain.dto.RoleQuery;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 角色管理集成测试。
 *
 * <p>初始数据:角色 1 超级管理员(admin)、角色 2 普通角色(common,已授权菜单 1/100/1000,已分配给用户 kite)。
 *
 * @author kite
 */
class SysRoleServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ISysRoleService sysRoleService;

    @Test
    @DisplayName("角色详情回填已授权菜单ID")
    void selectRoleByIdFillsMenuIds() {
        SysRole role = sysRoleService.selectRoleById(2L);

        assertThat(role.getRoleName()).isEqualTo("普通角色");
        assertThat(role.getMenuIds()).containsExactly(1L, 100L, 1000L);
    }

    @Test
    @DisplayName("分页查询支持按名称模糊与状态过滤")
    void pageWithFilters() {
        RoleQuery query = new RoleQuery();
        query.setRoleName("普通");

        PageResult<SysRole> page = sysRoleService.selectRolePage(query);

        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRows().get(0).getRoleKey()).isEqualTo("common");
    }

    @Test
    @DisplayName("分页参数非法时兜底为第一页")
    void pageFallsBackOnInvalidPageParams() {
        RoleQuery query = new RoleQuery();
        query.setPageNum(0L);
        query.setPageSize(-5L);

        PageResult<SysRole> page = sysRoleService.selectRolePage(query);

        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getRows()).hasSize(2);
    }

    @Test
    @DisplayName("新增角色并同时完成菜单授权")
    void insertRoleWithMenus() {
        RoleForm form = roleForm(null, "审计员", "auditor");
        form.setMenuIds(Arrays.asList(1L, 100L));

        Long newId = sysRoleService.insertRole(form);

        SysRole created = sysRoleService.selectRoleById(newId);
        assertThat(created.getMenuIds()).containsExactly(1L, 100L);
        assertThat(created.getStatus()).isZero();
        assertThat(created.getDataScope()).isEqualTo("1");
    }

    @Test
    @DisplayName("角色权限字符串重复被拒绝")
    void rejectsDuplicateRoleKey() {
        assertThatThrownBy(() -> sysRoleService.insertRole(roleForm(null, "另一个普通角色", "common")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("角色权限字符串");
    }

    @Test
    @DisplayName("角色名称重复被拒绝")
    void rejectsDuplicateRoleName() {
        assertThatThrownBy(() -> sysRoleService.insertRole(roleForm(null, "普通角色", "common2")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("角色名称");
    }

    @Test
    @DisplayName("授权采用覆盖语义:传入的集合就是最终结果")
    void assignMenusOverwrites() {
        sysRoleService.assignMenus(2L, Arrays.asList(1L, 101L));

        assertThat(sysRoleService.selectRoleById(2L).getMenuIds()).containsExactly(1L, 101L);
    }

    @Test
    @DisplayName("传空集合表示清空权限,传 null 表示不变更")
    void assignMenusDistinguishesEmptyFromNull() {
        sysRoleService.assignMenus(2L, Collections.emptyList());
        assertThat(sysRoleService.selectRoleById(2L).getMenuIds()).isEmpty();

        sysRoleService.assignMenus(2L, Arrays.asList(1L, 100L));
        sysRoleService.assignMenus(2L, null);
        assertThat(sysRoleService.selectRoleById(2L).getMenuIds()).containsExactly(1L, 100L);
    }

    @Test
    @DisplayName("重复提交同一菜单不会导致主键冲突")
    void assignMenusDeduplicates() {
        sysRoleService.assignMenus(2L, Arrays.asList(100L, 100L, 1L, 1L));

        assertThat(sysRoleService.selectRoleById(2L).getMenuIds()).containsExactly(1L, 100L);
    }

    @Test
    @DisplayName("授权不存在的菜单被拒绝")
    void rejectsUnknownMenu() {
        assertThatThrownBy(() -> sysRoleService.assignMenus(2L, Arrays.asList(1L, 99999L)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在或已删除的菜单");
    }

    @Test
    @DisplayName("超级管理员角色的权限字符串不允许修改")
    void protectsAdminRoleKey() {
        RoleForm form = roleForm(1L, "超级管理员", "not-admin");

        assertThatThrownBy(() -> sysRoleService.updateRole(form))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许修改");
    }

    @Test
    @DisplayName("超级管理员角色不允许删除或停用")
    void protectsAdminRole() {
        assertThatThrownBy(() -> sysRoleService.deleteRoles(Collections.singletonList(1L)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许删除");

        assertThatThrownBy(() -> sysRoleService.updateRoleStatus(1L, 1))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许停用");
    }

    @Test
    @DisplayName("已分配给用户的角色不允许删除")
    void rejectsDeleteWhenAssignedToUser() {
        assertThatThrownBy(() -> sysRoleService.deleteRoles(Collections.singletonList(2L)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已分配给用户");
    }

    @Test
    @DisplayName("未分配用户的角色可以删除,同时清理菜单授权")
    void deletesUnassignedRole() {
        RoleForm form = roleForm(null, "临时角色", "temp");
        form.setMenuIds(Collections.singletonList(1L));
        Long roleId = sysRoleService.insertRole(form);

        sysRoleService.deleteRoles(Collections.singletonList(roleId));

        assertThatThrownBy(() -> sysRoleService.selectRoleById(roleId))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("启用角色列表不包含已停用角色")
    void enabledRolesExcludeDisabled() {
        sysRoleService.updateRoleStatus(2L, 1);

        assertThat(sysRoleService.selectEnabledRoles())
                .extracting(SysRole::getRoleKey)
                .containsExactly("admin");
    }

    @Test
    @DisplayName("按用户查询其拥有的角色")
    void selectRolesByUserId() {
        assertThat(sysRoleService.selectRolesByUserId(1L))
                .extracting(SysRole::getRoleKey)
                .containsExactly("admin");
        assertThat(sysRoleService.selectRolesByUserId(2L))
                .extracting(SysRole::getRoleKey)
                .containsExactly("common");
    }

    private RoleForm roleForm(Long roleId, String roleName, String roleKey) {
        RoleForm form = new RoleForm();
        form.setRoleId(roleId);
        form.setRoleName(roleName);
        form.setRoleKey(roleKey);
        form.setRoleSort(9);
        return form;
    }
}
