package com.kite.libai.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.dto.UserQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper。
 *
 * @author kite
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 用户分页列表,关联部门表带出部门名称。
     *
     * <p>注意:{@code @TableLogic} 只对 MyBatis-Plus 自动生成的 SQL 生效,
     * 手写 XML 必须自行加上 {@code del_flag = 0} 过滤条件。
     *
     * @param page  分页对象,由分页插件自动改写 SQL 并回填 total
     * @param query 查询条件
     */
    IPage<SysUser> selectUserPage(IPage<SysUser> page, @Param("query") UserQuery query);
}
