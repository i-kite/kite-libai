package com.kite.libai.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.kite.libai.system.domain.SysDept;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 树形构建工具单元测试,不依赖 Spring 与数据库。
 *
 * @author kite
 */
class TreeUtilsTest {

    @Test
    void buildsMultiLevelTree() {
        List<SysDept> flat = Arrays.asList(
                dept(1L, 0L, "总公司"),
                dept(2L, 1L, "研发部门"),
                dept(3L, 1L, "市场部门"),
                dept(4L, 2L, "前端组"));

        List<SysDept> roots = build(flat);

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getDeptName()).isEqualTo("总公司");
        assertThat(roots.get(0).getChildren()).hasSize(2);

        SysDept rd = roots.get(0).getChildren().get(0);
        assertThat(rd.getDeptName()).isEqualTo("研发部门");
        assertThat(rd.getChildren()).hasSize(1);
        assertThat(rd.getChildren().get(0).getDeptName()).isEqualTo("前端组");
    }

    @Test
    void treatsNodesWithMissingParentAsRoots() {
        // 模拟按名称过滤后上级被筛掉的情况:子节点不能凭空消失,应升级为根节点
        List<SysDept> flat = Arrays.asList(
                dept(2L, 1L, "研发部门"),
                dept(4L, 2L, "前端组"));

        List<SysDept> roots = build(flat);

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getDeptName()).isEqualTo("研发部门");
        assertThat(roots.get(0).getChildren()).hasSize(1);
    }

    @Test
    void returnsEmptyListForEmptyInput() {
        assertThat(build(Collections.emptyList())).isEmpty();
        assertThat(TreeUtils.build(null, SysDept::getDeptId, SysDept::getParentId, SysDept::setChildren)).isEmpty();
    }

    @Test
    void keepsInputOrderOfRoots() {
        List<SysDept> flat = Arrays.asList(
                dept(1L, 0L, "甲公司"),
                dept(2L, 0L, "乙公司"),
                dept(3L, 0L, "丙公司"));

        assertThat(build(flat))
                .extracting(SysDept::getDeptName)
                .containsExactly("甲公司", "乙公司", "丙公司");
    }

    private List<SysDept> build(List<SysDept> flat) {
        return TreeUtils.build(new ArrayList<>(flat),
                SysDept::getDeptId, SysDept::getParentId, SysDept::setChildren);
    }

    private SysDept dept(Long id, Long parentId, String name) {
        SysDept dept = new SysDept();
        dept.setDeptId(id);
        dept.setParentId(parentId);
        dept.setDeptName(name);
        return dept;
    }
}
