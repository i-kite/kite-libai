package com.kite.libai.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.common.util.TreeUtils;
import com.kite.libai.system.domain.SysDept;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.dto.DeptForm;
import com.kite.libai.system.mapper.SysDeptMapper;
import com.kite.libai.system.mapper.SysUserMapper;
import com.kite.libai.system.service.ISysDeptService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 部门管理 Service 实现。
 *
 * @author kite
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysUserMapper sysUserMapper;

    @Autowired
    public SysDeptServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<SysDept> selectDeptList(String deptName, Integer status) {
        return list(Wrappers.<SysDept>lambdaQuery()
                .like(StringUtils.hasText(deptName), SysDept::getDeptName, deptName)
                .eq(status != null, SysDept::getStatus, status)
                .orderByAsc(SysDept::getParentId)
                .orderByAsc(SysDept::getOrderNum));
    }

    @Override
    public List<SysDept> selectDeptTree(String deptName, Integer status) {
        List<SysDept> flatList = selectDeptList(deptName, status);
        return TreeUtils.build(flatList, SysDept::getDeptId, SysDept::getParentId, SysDept::setChildren);
    }

    @Override
    public SysDept selectDeptById(Long deptId) {
        SysDept dept = getById(deptId);
        if (dept == null) {
            throw ServiceException.notFound("部门不存在或已被删除");
        }
        return dept;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertDept(DeptForm form) {
        checkParentAvailable(form.getParentId());
        checkDeptNameUnique(form.getParentId(), form.getDeptName(), null);

        SysDept dept = new SysDept();
        copyFormToEntity(form, dept);
        dept.setDeptId(null);
        dept.setAncestors(buildAncestors(form.getParentId()));
        if (dept.getStatus() == null) {
            dept.setStatus(SysConstants.STATUS_NORMAL);
        }
        save(dept);
        return dept.getDeptId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptForm form) {
        if (form.getDeptId() == null) {
            throw ServiceException.badRequest("部门ID不能为空");
        }
        SysDept current = selectDeptById(form.getDeptId());

        checkParentLegal(form.getDeptId(), form.getParentId());
        checkParentAvailable(form.getParentId());
        checkDeptNameUnique(form.getParentId(), form.getDeptName(), form.getDeptId());

        String newAncestors = buildAncestors(form.getParentId());
        String oldAncestors = current.getAncestors();

        SysDept dept = new SysDept();
        copyFormToEntity(form, dept);
        dept.setDeptId(form.getDeptId());
        dept.setAncestors(newAncestors);
        updateById(dept);

        // 上级发生变化时,子孙部门的祖级列表必须同步重写,否则按部门查询用户会漏数据
        if (!newAncestors.equals(oldAncestors)) {
            updateDescendantAncestors(form.getDeptId(), oldAncestors, newAncestors);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long deptId) {
        selectDeptById(deptId);

        long childCount = count(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getParentId, deptId));
        if (childCount > 0) {
            throw ServiceException.badRequest("该部门下仍存在下级部门,不允许删除");
        }

        long userCount = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeptId, deptId));
        if (userCount > 0) {
            throw ServiceException.badRequest("该部门下已分配用户,不允许删除");
        }

        removeById(deptId);
    }

    /**
     * 校验上级部门存在且处于启用状态。
     */
    private void checkParentAvailable(Long parentId) {
        if (SysConstants.ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        SysDept parent = getById(parentId);
        if (parent == null) {
            throw ServiceException.badRequest("上级部门不存在或已被删除");
        }
        if (SysConstants.STATUS_DISABLED.equals(parent.getStatus())) {
            throw ServiceException.badRequest("上级部门已停用,不允许在其下新增或挂载部门");
        }
    }

    /**
     * 校验上级部门的合法性:既不能是自己,也不能是自己的子孙,否则会形成环。
     */
    private void checkParentLegal(Long deptId, Long parentId) {
        if (deptId.equals(parentId)) {
            throw ServiceException.badRequest("上级部门不能是自己");
        }
        if (SysConstants.ROOT_PARENT_ID.equals(parentId)) {
            return;
        }
        SysDept parent = getById(parentId);
        if (parent != null && containsAncestor(parent.getAncestors(), deptId)) {
            throw ServiceException.badRequest("上级部门不能是自己的下级部门");
        }
    }

    /**
     * 判断祖级列表中是否包含指定部门ID。
     *
     * <p>前后补逗号再匹配,避免 "0,1" 被 "0,11" 误命中。
     */
    private boolean containsAncestor(String ancestors, Long deptId) {
        if (!StringUtils.hasText(ancestors)) {
            return false;
        }
        String wrapped = SysConstants.ANCESTORS_SEPARATOR + ancestors + SysConstants.ANCESTORS_SEPARATOR;
        return wrapped.contains(SysConstants.ANCESTORS_SEPARATOR + deptId + SysConstants.ANCESTORS_SEPARATOR);
    }

    /**
     * 根据上级部门推导祖级列表。
     */
    private String buildAncestors(Long parentId) {
        if (SysConstants.ROOT_PARENT_ID.equals(parentId)) {
            return String.valueOf(SysConstants.ROOT_PARENT_ID);
        }
        SysDept parent = getById(parentId);
        if (parent == null) {
            throw ServiceException.badRequest("上级部门不存在或已被删除");
        }
        return parent.getAncestors() + SysConstants.ANCESTORS_SEPARATOR + parentId;
    }

    /**
     * 级联重写子孙部门的祖级列表。
     *
     * <p>子孙的 ancestors 一定以"本部门旧祖级 + 本部门ID"为前缀,按前缀替换即可,
     * 不能直接用字符串 replace,否则可能命中路径中间的同名片段。
     */
    private void updateDescendantAncestors(Long deptId, String oldAncestors, String newAncestors) {
        String oldPrefix = oldAncestors + SysConstants.ANCESTORS_SEPARATOR + deptId;
        String newPrefix = newAncestors + SysConstants.ANCESTORS_SEPARATOR + deptId;

        for (SysDept descendant : selectDescendants(deptId)) {
            String ancestors = descendant.getAncestors();
            if (ancestors == null || !ancestors.startsWith(oldPrefix)) {
                continue;
            }
            SysDept update = new SysDept();
            update.setDeptId(descendant.getDeptId());
            update.setAncestors(newPrefix + ancestors.substring(oldPrefix.length()));
            updateById(update);
        }
    }

    /**
     * 查询指定部门的所有子孙部门。
     *
     * <p>用 ancestors 做一次 LIKE 即可,避免逐层递归查库。
     * 这里不用 MySQL 的 FIND_IN_SET,以便同一套 SQL 能在 H2 测试库中运行。
     */
    private List<SysDept> selectDescendants(Long deptId) {
        String pattern = "%" + SysConstants.ANCESTORS_SEPARATOR + deptId + SysConstants.ANCESTORS_SEPARATOR + "%";
        return list(Wrappers.<SysDept>lambdaQuery()
                .apply("concat(',', ancestors, ',') like {0}", pattern));
    }

    /**
     * 校验同级部门下名称唯一。
     *
     * @param excludeDeptId 需要排除的部门ID,修改场景下传入自身ID
     */
    private void checkDeptNameUnique(Long parentId, String deptName, Long excludeDeptId) {
        long count = count(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getParentId, parentId)
                .eq(SysDept::getDeptName, deptName)
                .ne(excludeDeptId != null, SysDept::getDeptId, excludeDeptId));
        if (count > 0) {
            throw ServiceException.badRequest("同级部门下已存在名称为「" + deptName + "」的部门");
        }
    }

    private void copyFormToEntity(DeptForm form, SysDept dept) {
        dept.setParentId(form.getParentId());
        dept.setDeptName(form.getDeptName());
        dept.setOrderNum(form.getOrderNum());
        dept.setLeader(form.getLeader());
        dept.setPhone(form.getPhone());
        dept.setEmail(form.getEmail());
        dept.setStatus(form.getStatus());
        dept.setRemark(form.getRemark());
    }
}
