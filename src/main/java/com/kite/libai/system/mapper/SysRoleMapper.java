package com.kite.libai.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kite.libai.system.domain.SysRole;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 角色 Mapper。
 *
 * @author kite
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询指定用户拥有的角色列表。
     *
     * @param userId 用户ID
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);
}
