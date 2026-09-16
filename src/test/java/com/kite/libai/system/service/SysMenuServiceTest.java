package com.kite.libai.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kite.libai.AbstractIntegrationTest;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.system.domain.SysMenu;
import com.kite.libai.system.domain.dto.MenuForm;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 菜单权限管理集成测试。
 *
 * <p>初始数据:1 系统管理(M) 下挂 100 用户管理 / 101 角色管理 / 102 菜单管理 / 103 部门管理(C),
 * 每个 C 下再挂若干 F 按钮;菜单 1000(用户查询)已授权给角色 2。
 *
 * @author kite
 */
class SysMenuServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ISysMenuService sysMenuService;

    @Test
    @DisplayName("菜单树按目录-菜单-按钮三层组装")
    void buildsMenuTree() {
        List<SysMenu> tree = sysMenuService.selectMenuTree(null, null);

        assertThat(tree).hasSize(1);
        SysMenu root = tree.get(0);
        assertThat(root.getMenuName()).isEqualTo("系统管理");
        assertThat(root.getMenuType()).isEqualTo("M");
        assertThat(root.getChildren()).extracting(SysMenu::getMenuName)
                .containsExactly("用户管理", "角色管理", "菜单管理", "部门管理");

        SysMenu userMenu = root.getChildren().get(0);
        assertThat(userMenu.getChildren()).extracting(SysMenu::getMenuName)
                .containsExactly("用户查询", "用户新增", "用户修改", "用户删除", "重置密码");
        assertThat(userMenu.getChildren()).extracting(SysMenu::getMenuType)
                .containsOnly("F");
    }

    @Test
    @DisplayName("新增按钮类型菜单")
    void insertButtonMenu() {
        MenuForm form = menuForm(null, 103L, "部门导出", "F");
        form.setPerms("system:dept:export");

        Long newId = sysMenuService.insertMenu(form);

        SysMenu created = sysMenuService.selectMenuById(newId);
        assertThat(created.getMenuType()).isEqualTo("F");
        assertThat(created.getPerms()).isEqualTo("system:dept:export");
        // 未传值时的默认项
        assertThat(created.getStatus()).isZero();
        assertThat(created.getVisible()).isZero();
        assertThat(created.getIsFrame()).isEqualTo(1);
    }

    @Test
    @DisplayName("权限标识全局唯一")
    void rejectsDuplicatePerms() {
        MenuForm form = menuForm(null, 103L, "另一个用户查询", "F");
        form.setPerms("system:user:query");

        assertThatThrownBy(() -> sysMenuService.insertMenu(form))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("权限标识");
    }

    @Test
    @DisplayName("同级菜单名称唯一")
    void rejectsDuplicateNameAtSameLevel() {
        assertThatThrownBy(() -> sysMenuService.insertMenu(menuForm(null, 1L, "用户管理", "C")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已存在名称为");
    }

    @Test
    @DisplayName("上级菜单不存在时拒绝")
    void rejectsUnknownParent() {
        assertThatThrownBy(() -> sysMenuService.insertMenu(menuForm(null, 88888L, "野菜单", "C")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("上级菜单不存在");
    }

    @Test
    @DisplayName("上级菜单不能是自己")
    void rejectsSelfAsParent() {
        assertThatThrownBy(() -> sysMenuService.updateMenu(menuForm(100L, 100L, "用户管理", "C")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能是自己");
    }

    @Test
    @DisplayName("存在子菜单时拒绝删除")
    void rejectsDeleteWhenChildrenExist() {
        assertThatThrownBy(() -> sysMenuService.deleteMenu(100L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("仍存在子菜单");
    }

    @Test
    @DisplayName("已被角色引用时拒绝删除")
    void rejectsDeleteWhenAssignedToRole() {
        // 菜单 1000(用户查询)已授权给角色 2
        assertThatThrownBy(() -> sysMenuService.deleteMenu(1000L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已被角色引用");
    }

    @Test
    @DisplayName("无子菜单且未被引用的菜单可以删除")
    void deletesUnusedMenu() {
        // 菜单 1016(部门删除按钮)无子节点,也未授权给任何角色
        sysMenuService.deleteMenu(1016L);

        assertThatThrownBy(() -> sysMenuService.selectMenuById(1016L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("修改菜单后字段生效")
    void updateMenu() {
        MenuForm form = menuForm(103L, 1L, "组织架构", "C");
        form.setPerms("system:dept:list");
        form.setIcon("tree");

        sysMenuService.updateMenu(form);

        SysMenu updated = sysMenuService.selectMenuById(103L);
        assertThat(updated.getMenuName()).isEqualTo("组织架构");
        assertThat(updated.getIcon()).isEqualTo("tree");
    }

    @Test
    @DisplayName("按名称模糊过滤后,上级被筛掉的节点升级为根节点")
    void filterKeepsMatchedNodesAsRoots() {
        List<SysMenu> tree = sysMenuService.selectMenuTree("用户", null);

        // 命中的是 "用户管理" 与它下面 4 个名字带"用户"的按钮("重置密码"不含"用户"故未命中)。
        // 父级 "系统管理" 未命中被筛掉,"用户管理" 因此升级为根节点,不会凭空消失。
        assertThat(tree).extracting(SysMenu::getMenuName).containsExactly("用户管理");
        assertThat(tree.get(0).getChildren())
                .extracting(SysMenu::getMenuName)
                .containsExactly("用户查询", "用户新增", "用户修改", "用户删除");
    }

    private MenuForm menuForm(Long menuId, Long parentId, String menuName, String menuType) {
        MenuForm form = new MenuForm();
        form.setMenuId(menuId);
        form.setParentId(parentId);
        form.setMenuName(menuName);
        form.setMenuType(menuType);
        form.setOrderNum(9);
        return form;
    }
}
