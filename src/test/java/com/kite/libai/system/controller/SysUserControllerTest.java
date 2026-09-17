package com.kite.libai.system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kite.libai.AbstractIntegrationTest;
import com.kite.libai.system.domain.dto.UserForm;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 用户管理接口测试,走完整的 MVC 链路(参数绑定、校验、全局异常处理、JSON 序列化)。
 *
 * @author kite
 */
class SysUserControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("分页接口返回统一 R 结构")
    void pageReturnsUnifiedEnvelope() throws Exception {
        mockMvc.perform(get("/api/system/user/page").header("Authorization", adminToken()).param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("操作成功"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.rows.length()").value(2));
    }

    @Test
    @DisplayName("详情接口不返回密码字段")
    void detailNeverExposesPassword() throws Exception {
        mockMvc.perform(get("/api/system/user/{userId}", 1).header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("admin"))
                .andExpect(jsonPath("$.data.deptName").value("研发部门"))
                .andExpect(jsonPath("$.data.roleIds[0]").value(1))
                // password 上标注了 @JsonIgnore,任何情况下都不应出现在响应里
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("用户不存在时返回 404")
    void detailReturns404WhenMissing() throws Exception {
        mockMvc.perform(get("/api/system/user/{userId}", 99999).header("Authorization", adminToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("参数校验失败返回 400 并带上具体字段提示")
    void validationFailureReturns400() throws Exception {
        UserForm form = new UserForm();
        form.setUserName("ab");
        // 缺少 nickName,且密码太短

        form.setPassword("123");

        mockMvc.perform(post("/api/system/user")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("nickName")));
    }

    @Test
    @DisplayName("登录账号含非法字符时返回 400")
    void rejectsIllegalUserName() throws Exception {
        UserForm form = new UserForm();
        form.setUserName("bad name!");
        form.setNickName("非法账号");
        form.setPassword("abc123456");

        mockMvc.perform(post("/api/system/user")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("userName")));
    }

    @Test
    @DisplayName("新增用户成功返回新ID")
    void addUserSuccessfully() throws Exception {
        UserForm form = new UserForm();
        form.setUserName("newbie");
        form.setNickName("新同事");
        form.setDeptId(2L);
        form.setPassword("newbie123");
        form.setRoleIds(Collections.singletonList(2L));

        mockMvc.perform(post("/api/system/user")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("新增成功"))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("业务规则拒绝删除超级管理员,返回 400 与中文提示")
    void deleteAdminRejected() throws Exception {
        mockMvc.perform(delete("/api/system/user/{userIds}", "1").header("Authorization", adminToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("超级管理员账号不允许删除"));
    }

    @Test
    @DisplayName("修改状态接口生效")
    void changeStatus() throws Exception {
        mockMvc.perform(put("/api/system/user/{userId}/status", 2).header("Authorization", adminToken()).param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("状态修改成功"));

        mockMvc.perform(get("/api/system/user/{userId}", 2).header("Authorization", adminToken()))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    @DisplayName("按部门筛选时连带子孙部门的用户")
    void pageFilteredByDeptIncludesDescendants() throws Exception {
        mockMvc.perform(get("/api/system/user/page").header("Authorization", adminToken()).param("deptId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].userName").value("kite"));

        mockMvc.perform(get("/api/system/user/page").header("Authorization", adminToken()).param("deptId", "1"))
                .andExpect(jsonPath("$.data.total").value(2));
    }
}
