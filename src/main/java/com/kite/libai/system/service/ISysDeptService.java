package com.kite.libai.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kite.libai.system.domain.SysDept;
import com.kite.libai.system.domain.dto.DeptForm;
import java.util.List;

/**
 * 部门管理 Service。
 *
 * @author kite
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 查询部门平铺列表。
     *
     * @param deptName 部门名称,模糊匹配,可为空
     * @param status   部门状态,可为空
     */
    List<SysDept> selectDeptList(String deptName, Integer status);

    /**
     * 查询部门树。
     *
     * @param deptName 部门名称,模糊匹配,可为空
     * @param status   部门状态,可为空
     */
    List<SysDept> selectDeptTree(String deptName, Integer status);

    /**
     * 按ID查询部门,不存在时抛出业务异常。
     *
     * @param deptId 部门ID
     */
    SysDept selectDeptById(Long deptId);

    /**
     * 新增部门。
     *
     * @param form 部门表单
     * @return 新部门ID
     */
    Long insertDept(DeptForm form);

    /**
     * 修改部门。变更上级部门时会级联维护所有子孙部门的祖级列表。
     *
     * @param form 部门表单
     */
    void updateDept(DeptForm form);

    /**
     * 删除部门。存在下级部门或已分配用户时拒绝删除。
     *
     * @param deptId 部门ID
     */
    void deleteDept(Long deptId);
}
