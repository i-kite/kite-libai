package com.kite.libai.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.util.UserContext;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 审计字段自动填充。
 *
 * <p>使用 {@code strictInsertFill} / {@code strictUpdateFill},只在字段为空时填充,
 * 不会覆盖业务代码显式设置的值(例如数据迁移场景需要保留原始创建时间)。
 *
 * @author kite
 */
@Component
public class MybatisAuditHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String operator = UserContext.getUsername();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, operator);
        this.strictInsertFill(metaObject, "updateBy", String.class, operator);
        // 逻辑删除字段给默认值,避免依赖数据库 DEFAULT 定义
        this.strictInsertFill(metaObject, "delFlag", Integer.class, SysConstants.NOT_DELETED);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新场景必须覆盖旧值,所以用 setFieldValByName 而非 strictUpdateFill
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateBy", UserContext.getUsername(), metaObject);
    }
}
