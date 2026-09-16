package com.kite.libai;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据库集成测试基类。
 *
 * <p>说明:
 * <ul>
 *   <li>数据源为 H2 内存库(MySQL 兼容模式),建表与初始化数据直接复用生产脚本
 *       {@code db/mysql/schema.sql} 与 {@code db/mysql/data.sql},配置见
 *       {@code src/test/resources/application.yml}</li>
 *   <li>类上的 {@code @Transactional} 使每个测试方法结束后自动回滚,
 *       测试之间互不污染,因此可以放心增删改初始化数据</li>
 *   <li>所有子类共享同一份注解组合,Spring 测试上下文只会创建一次并被缓存复用</li>
 * </ul>
 *
 * @author kite
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {
}
