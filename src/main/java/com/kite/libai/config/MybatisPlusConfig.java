package com.kite.libai.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置。
 *
 * @author kite
 */
@Configuration
@MapperScan("com.kite.libai.system.mapper")
public class MybatisPlusConfig {

    /** 单页最大条数,超出后分页插件自动截断,防止恶意大分页 */
    private static final long MAX_PAGE_LIMIT = 500L;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 这里刻意不指定 DbType,让插件按连接 URL 自动识别方言。
        // 生产是 MySQL,测试是 H2,写死 MySQL 会导致测试环境分页 SQL 语法不匹配。
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor();
        pagination.setMaxLimit(MAX_PAGE_LIMIT);
        // 页码超过总页数时返回空列表,而不是回到第一页,避免前端误判
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);

        // 拦截没有 where 条件的全表更新与删除,兜底防止误操作
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
