package com.kite.libai.system.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kite.libai.AbstractIntegrationTest;
import com.kite.libai.system.domain.SysMenu;
import com.kite.libai.system.domain.vo.UserPermissionVO;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 权限聚合查询集成测试,覆盖 用户 -> 角色 -> 菜单/权限标识 的完整链路。
 *
 * @author kite
 */
class SysPermissionServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ISysPermissionService sysPermissionService;

    @Autowired
    private ISysRoleService sysRoleService;

    @Test
    @DisplayName("超级管理员返回权限通配符与全部菜单")
    void superAdminGetsWildcardAndAllMenus() {
        UserPermissionVO vo = sysPermissionService.getUserPermission(1L);

        assertThat(vo.getUserName()).isEqualTo("admin");
        assertThat(vo.isAdmin()).isTrue();
        assertThat(vo.getRoles()).containsExactly("admin");
        assertThat(vo.getPermissions()).containsExactly("*:*:*");

        // 菜单树只含目录与菜单,按钮不参与前端路由
        assertThat(vo.getMenus()).hasSize(1);
        SysMenu root = vo.getMenus().get(0);
        assertThat(root.getMenuName()).isEqualTo("系统管理");
        assertThat(root.getChildren()).hasSize(4);
        assertThat(root.getChildren()).allSatisfy(child ->
                assertThat(child.getChildren()).isEmpty());
    }

    @Test
    @DisplayName("普通用户只返回已授权的权限标识与菜单")
    void normalUserGetsOnlyGrantedPermissions() {
        UserPermissionVO vo = sysPermissionService.getUserPermission(2L);

        assertThat(vo.getUserName()).isEqualTo("kite");
        assertThat(vo.isAdmin()).isFalse();
        assertThat(vo.getRoles()).containsExactly("common");
        // 角色 2 被授予菜单 1(系统管理,目录无 perms)、100(用户管理,perms=system:user:list)、
        // 1000(用户查询按钮,perms=system:user:query)。
        // 注意 C 类型菜单本身也带权限标识,不是只有 F 类型按钮才有 perms。
        assertThat(vo.getPermissions())
                .containsExactlyInAnyOrder("system:user:list", "system:user:query");

        assertThat(vo.getMenus()).hasSize(1);
        assertThat(vo.getMenus().get(0).getMenuName()).isEqualTo("系统管理");
        assertThat(vo.getMenus().get(0).getChildren())
                .extracting(SysMenu::getMenuName)
                .containsExactly("用户管理");
    }

    @Test
    @DisplayName("重新授权后权限标识立即变化")
    void permissionsReflectReassignment() {
        sysRoleService.assignMenus(2L, Arrays.asList(1L, 101L, 1005L, 1006L));

        UserPermissionVO vo = sysPermissionService.getUserPermission(2L);

        // 菜单 101 角色管理(system:role:list)+ 1005 角色查询 + 1006 角色新增
        assertThat(vo.getPermissions())
                .containsExactlyInAnyOrder("system:role:list", "system:role:query", "system:role:add");
        assertThat(vo.getMenus().get(0).getChildren())
                .extracting(SysMenu::getMenuName)
                .containsExactly("角色管理");
    }

    @Test
    @DisplayName("角色被停用后其权限立即失效")
    void disabledRoleLosesPermissions() {
        sysRoleService.updateRoleStatus(2L, 1);

        UserPermissionVO vo = sysPermissionService.getUserPermission(2L);

        assertThat(vo.getRoles()).isEmpty();
        assertThat(vo.isAdmin()).isFalse();
        assertThat(vo.getPermissions()).isEmpty();
        assertThat(vo.getMenus()).isEmpty();
    }

    @Test
    @DisplayName("清空授权后无任何权限")
    void noPermissionsAfterRevokeAll() {
        sysRoleService.assignMenus(2L, Collections.emptyList());

        UserPermissionVO vo = sysPermissionService.getUserPermission(2L);

        assertThat(vo.getPermissions()).isEmpty();
        assertThat(vo.getMenus()).isEmpty();
    }
}
