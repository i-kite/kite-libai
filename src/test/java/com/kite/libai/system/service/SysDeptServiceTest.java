package com.kite.libai.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kite.libai.AbstractIntegrationTest;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.system.domain.SysDept;
import com.kite.libai.system.domain.dto.DeptForm;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 部门管理集成测试。
 *
 * <p>初始数据结构(来自 db/mysql/data.sql):
 * <pre>
 * 1 总公司 (ancestors=0)
 *   ├─ 2 研发部门 (0,1)
 *   │    ├─ 4 前端组 (0,1,2)   ← 用户 kite 在此
 *   │    └─ 5 后端组 (0,1,2)
 *   └─ 3 市场部门 (0,1)
 * </pre>
 *
 * @author kite
 */
class SysDeptServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ISysDeptService sysDeptService;

    @Test
    @DisplayName("部门树按层级正确组装")
    void buildsDeptTree() {
        List<SysDept> tree = sysDeptService.selectDeptTree(null, null);

        assertThat(tree).hasSize(1);
        SysDept root = tree.get(0);
        assertThat(root.getDeptName()).isEqualTo("总公司");
        assertThat(root.getChildren()).extracting(SysDept::getDeptName)
                .containsExactly("研发部门", "市场部门");
        assertThat(root.getChildren().get(0).getChildren()).extracting(SysDept::getDeptName)
                .containsExactly("前端组", "后端组");
    }

    @Test
    @DisplayName("新增部门时自动推导祖级列表")
    void insertDeptCalculatesAncestors() {
        DeptForm form = deptForm(null, 2L, "测试组");

        Long newId = sysDeptService.insertDept(form);

        SysDept created = sysDeptService.selectDeptById(newId);
        assertThat(created.getAncestors()).isEqualTo("0,1,2");
        assertThat(created.getStatus()).isZero();
        // 审计字段由 MetaObjectHandler 自动填充
        assertThat(created.getCreateBy()).isEqualTo("system");
        assertThat(created.getCreateTime()).isNotNull();
    }

    @Test
    @DisplayName("顶级部门的祖级列表为 0")
    void insertTopLevelDept() {
        Long newId = sysDeptService.insertDept(deptForm(null, 0L, "分公司"));

        assertThat(sysDeptService.selectDeptById(newId).getAncestors()).isEqualTo("0");
    }

    @Test
    @DisplayName("同级部门重名被拒绝,不同级可以同名")
    void rejectsDuplicateNameAtSameLevel() {
        assertThatThrownBy(() -> sysDeptService.insertDept(deptForm(null, 2L, "前端组")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已存在名称为");

        // 挂在市场部门下的"前端组"属于不同父级,应当允许
        Long newId = sysDeptService.insertDept(deptForm(null, 3L, "前端组"));
        assertThat(newId).isNotNull();
    }

    @Test
    @DisplayName("变更上级部门时级联重写所有子孙的祖级列表")
    void updateDeptCascadesAncestors() {
        // 把研发部门(2)从总公司(1)下挪到市场部门(3)下
        DeptForm form = deptForm(2L, 3L, "研发部门");

        sysDeptService.updateDept(form);

        assertThat(sysDeptService.selectDeptById(2L).getAncestors()).isEqualTo("0,1,3");
        // 前端组与后端组作为子孙必须同步更新,否则按部门查用户会漏数据
        assertThat(sysDeptService.selectDeptById(4L).getAncestors()).isEqualTo("0,1,3,2");
        assertThat(sysDeptService.selectDeptById(5L).getAncestors()).isEqualTo("0,1,3,2");
    }

    @Test
    @DisplayName("上级部门不能是自己")
    void rejectsSelfAsParent() {
        assertThatThrownBy(() -> sysDeptService.updateDept(deptForm(2L, 2L, "研发部门")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能是自己");
    }

    @Test
    @DisplayName("上级部门不能是自己的下级,避免形成环")
    void rejectsDescendantAsParent() {
        // 试图把总公司(1)挂到它的孙子前端组(4)下面
        assertThatThrownBy(() -> sysDeptService.updateDept(deptForm(1L, 4L, "总公司")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能是自己的下级部门");
    }

    @Test
    @DisplayName("存在下级部门时拒绝删除")
    void rejectsDeleteWhenChildrenExist() {
        assertThatThrownBy(() -> sysDeptService.deleteDept(2L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("仍存在下级部门");
    }

    @Test
    @DisplayName("部门下已分配用户时拒绝删除")
    void rejectsDeleteWhenUsersAssigned() {
        // 前端组(4)下有用户 kite
        assertThatThrownBy(() -> sysDeptService.deleteDept(4L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已分配用户");
    }

    @Test
    @DisplayName("无下级且无用户的部门可以删除,且删除后查询不到")
    void deletesLeafDept() {
        // 后端组(5)既无下级也无用户
        sysDeptService.deleteDept(5L);

        assertThatThrownBy(() -> sysDeptService.selectDeptById(5L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        // 逻辑删除:记录仍在库中,但已被自动过滤
        assertThat(sysDeptService.selectDeptList(null, null))
                .extracting(SysDept::getDeptId)
                .doesNotContain(5L);
    }

    @Test
    @DisplayName("上级部门已停用时不允许在其下新增")
    void rejectsInsertUnderDisabledParent() {
        DeptForm disable = deptForm(3L, 1L, "市场部门");
        disable.setStatus(1);
        sysDeptService.updateDept(disable);

        assertThatThrownBy(() -> sysDeptService.insertDept(deptForm(null, 3L, "渠道组")))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已停用");
    }

    @Test
    @DisplayName("按名称模糊查询")
    void filtersByName() {
        assertThat(sysDeptService.selectDeptList("组", null))
                .extracting(SysDept::getDeptName)
                .containsExactlyInAnyOrder("前端组", "后端组");
    }

    private DeptForm deptForm(Long deptId, Long parentId, String deptName) {
        DeptForm form = new DeptForm();
        form.setDeptId(deptId);
        form.setParentId(parentId);
        form.setDeptName(deptName);
        form.setOrderNum(1);
        form.setLeader("负责人");
        return form;
    }
}
