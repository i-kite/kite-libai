package com.kite.libai.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kite.libai.common.core.domain.BaseEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门实体,对应表 sys_dept。
 *
 * @author kite
 */
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 部门ID */
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long deptId;

    /** 父部门ID,顶级为 0 */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 祖级列表,形如 "0,1,2"。
     *
     * <p>冗余存储的目的是把"查某部门下所有子孙"从递归查询降为一次 LIKE 查询。
     */
    @TableField("ancestors")
    private String ancestors;

    /** 部门名称 */
    @TableField("dept_name")
    private String deptName;

    /** 显示顺序 */
    @TableField("order_num")
    private Integer orderNum;

    /** 负责人 */
    @TableField("leader")
    private String leader;

    /** 联系电话 */
    @TableField("phone")
    private String phone;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 部门状态:0-正常 1-停用 */
    @TableField("status")
    private Integer status;

    /** 子部门列表,非数据库字段,仅在返回树形结构时填充 */
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<SysDept> getChildren() {
        return children;
    }

    public void setChildren(List<SysDept> children) {
        this.children = children;
    }
}
