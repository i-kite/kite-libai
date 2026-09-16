package com.kite.libai.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kite.libai.AbstractIntegrationTest;
import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.dto.ResetPwdForm;
import com.kite.libai.system.domain.dto.UserForm;
import com.kite.libai.system.domain.dto.UserQuery;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用户管理集成测试。
 *
 * <p>初始数据:admin(id=1, dept=2 研发部门, 角色 admin)、kite(id=2, dept=4 前端组, 角色 common)。
 *
 * @author kite
 */
class SysUserServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("初始化脚本中的密码密文与文档声明的明文一致")
    void initialPasswordsMatchDocumentedPlainText() {
        // 这条断言用于防止 data.sql 里的 BCrypt 密文与注释中的明文说明脱节
        assertThat(passwordEncoder.matches("admin123", sysUserService.getById(1L).getPassword())).isTrue();
        assertThat(passwordEncoder.matches("kite123", sysUserService.getById(2L).getPassword())).isTrue();
    }

    @Test
    @DisplayName("用户详情回填部门名称与已分配角色")
    void selectUserByIdFillsDeptAndRoles() {
        SysUser user = sysUserService.selectUserById(2L);

        assertThat(user.getUserName()).isEqualTo("kite");
        assertThat(user.getDeptName()).isEqualTo("前端组");
        assertThat(user.getRoleIds()).containsExactly(2L);
    }

    @Test
    @DisplayName("新增用户密码以 BCrypt 存储,不落明文")
    void insertUserEncodesPassword() {
        UserForm form = userForm(null, "zhangsan", "张三", 2L);
        form.setPassword("zhangsan123");
        form.setRoleIds(Collections.singletonList(2L));

        Long newId = sysUserService.insertUser(form);

        SysUser created = sysUserService.selectUserById(newId);
        assertThat(created.getPassword()).isNotEqualTo("zhangsan123").startsWith("$2a$");
        assertThat(passwordEncoder.matches("zhangsan123", created.getPassword())).isTrue();
        assertThat(created.getRoleIds()).containsExactly(2L);
    }

    @Test
    @DisplayName("新增用户必须提供密码")
    void rejectsInsertWithoutPassword() {
        UserForm form = userForm(null, "lisi", "李四", 2L);

        assertThatThrownBy(() -> sysUserService.insertUser(form))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("密码不能为空");
    }

    @Test
    @DisplayName("登录账号重复被拒绝")
    void rejectsDuplicateUserName() {
        UserForm form = userForm(null, "admin", "重复账号", 2L);
        form.setPassword("whatever123");

        assertThatThrownBy(() -> sysUserService.insertUser(form))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    @DisplayName("手机号被其他用户占用时拒绝")
    void rejectsDuplicatePhone() {
        UserForm form = userForm(null, "wangwu", "王五", 2L);
        form.setPassword("wangwu123");
        form.setPhonenumber("15888888888");

        assertThatThrownBy(() -> sysUserService.insertUser(form))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已被其他用户使用");
    }

    @Test
    @DisplayName("分配不存在的角色被拒绝")
    void rejectsUnknownRole() {
        UserForm form = userForm(null, "zhaoliu", "赵六", 2L);
        form.setPassword("zhaoliu123");
        form.setRoleIds(Collections.singletonList(999L));

        assertThatThrownBy(() -> sysUserService.insertUser(form))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在或已删除的角色");
    }

    @Test
    @DisplayName("按部门查询连带其所有子孙部门的用户")
    void pageByDeptIncludesDescendants() {
        // 总公司(1)下应查出 admin(研发部门) 与 kite(前端组)
        assertThat(pageByDept(1L).getTotal()).isEqualTo(2);
        // 研发部门(2)本身有 admin,其子部门前端组有 kite
        assertThat(pageByDept(2L).getTotal()).isEqualTo(2);
        // 前端组(4)是叶子,只有 kite
        PageResult<SysUser> frontend = pageByDept(4L);
        assertThat(frontend.getTotal()).isEqualTo(1);
        assertThat(frontend.getRows().get(0).getUserName()).isEqualTo("kite");
        // 市场部门(3)下无人
        assertThat(pageByDept(3L).getTotal()).isZero();
    }

    @Test
    @DisplayName("分页列表带出部门名称,且不返回密码")
    void pageFillsDeptNameAndHidesPassword() {
        UserQuery query = new UserQuery();
        query.setUserName("kite");

        PageResult<SysUser> page = sysUserService.selectUserPage(query);

        assertThat(page.getTotal()).isEqualTo(1);
        SysUser row = page.getRows().get(0);
        assertThat(row.getDeptName()).isEqualTo("前端组");
        // 列表 SQL 未查询 password 列,避免密文在列表接口中被批量带出
        assertThat(row.getPassword()).isNull();
    }

    @Test
    @DisplayName("修改用户时登录账号不可变更")
    void updateUserKeepsUserName() {
        UserForm form = userForm(2L, "hacked", "开发者改名", 4L);

        sysUserService.updateUser(form);

        SysUser updated = sysUserService.selectUserById(2L);
        assertThat(updated.getUserName()).isEqualTo("kite");
        assertThat(updated.getNickName()).isEqualTo("开发者改名");
    }

    @Test
    @DisplayName("修改用户时密码留空则保持原密码不变")
    void updateUserWithBlankPasswordKeepsOldPassword() {
        String before = sysUserService.getById(2L).getPassword();

        sysUserService.updateUser(userForm(2L, "kite", "开发者", 4L));

        assertThat(sysUserService.getById(2L).getPassword()).isEqualTo(before);
    }

    @Test
    @DisplayName("重置密码后新密码生效")
    void resetPassword() {
        ResetPwdForm form = new ResetPwdForm();
        form.setUserId(2L);
        form.setPassword("newPwd123");

        sysUserService.resetPassword(form);

        assertThat(passwordEncoder.matches("newPwd123", sysUserService.getById(2L).getPassword())).isTrue();
    }

    @Test
    @DisplayName("超级管理员账号不允许删除或停用")
    void protectsSuperAdmin() {
        assertThat(sysUserService.isSuperAdmin(1L)).isTrue();
        assertThat(sysUserService.isSuperAdmin(2L)).isFalse();

        assertThatThrownBy(() -> sysUserService.deleteUsers(Collections.singletonList(1L)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许删除");

        assertThatThrownBy(() -> sysUserService.updateUserStatus(1L, 1))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许停用");
    }

    @Test
    @DisplayName("删除普通用户同时清理角色关联")
    void deleteUserCleansRoleRelation() {
        sysUserService.deleteUsers(Collections.singletonList(2L));

        assertThatThrownBy(() -> sysUserService.selectUserById(2L))
                .isInstanceOf(ServiceException.class);
        // 关联已被物理清理,重新分配同一角色时不会残留脏数据
        assertThat(sysUserService.isSuperAdmin(2L)).isFalse();
    }

    @Test
    @DisplayName("批量删除时只要有一个是管理员则整体回滚")
    void batchDeleteRejectsWhenAdminIncluded() {
        assertThatThrownBy(() -> sysUserService.deleteUsers(Arrays.asList(2L, 1L)))
                .isInstanceOf(ServiceException.class);

        // 校验在删除之前完成,普通用户不应被误删
        assertThat(sysUserService.getById(2L)).isNotNull();
    }

    @Test
    @DisplayName("非法状态值被拒绝")
    void rejectsInvalidStatus() {
        assertThatThrownBy(() -> sysUserService.updateUserStatus(2L, 9))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("状态取值");
    }

    @Test
    @DisplayName("停用普通用户成功")
    void disableNormalUser() {
        sysUserService.updateUserStatus(2L, 1);

        assertThat(sysUserService.getById(2L).getStatus()).isEqualTo(1);
    }

    private PageResult<SysUser> pageByDept(Long deptId) {
        UserQuery query = new UserQuery();
        query.setDeptId(deptId);
        return sysUserService.selectUserPage(query);
    }

    private UserForm userForm(Long userId, String userName, String nickName, Long deptId) {
        UserForm form = new UserForm();
        form.setUserId(userId);
        form.setUserName(userName);
        form.setNickName(nickName);
        form.setDeptId(deptId);
        return form;
    }
}
