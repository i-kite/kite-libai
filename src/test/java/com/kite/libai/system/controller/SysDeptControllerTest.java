package com.kite.libai.system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kite.libai.AbstractIntegrationTest;
import com.kite.libai.system.domain.dto.DeptForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 部门管理接口测试。
 *
 * @author kite
 */
class SysDeptControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("部门树接口返回嵌套结构")
    void treeReturnsNestedStructure() throws Exception {
        mockMvc.perform(get("/api/system/dept/tree").header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].deptName").value("总公司"))
                .andExpect(jsonPath("$.data[0].children.length()").value(2))
                .andExpect(jsonPath("$.data[0].children[0].deptName").value("研发部门"))
                .andExpect(jsonPath("$.data[0].children[0].children[0].deptName").value("前端组"));
    }

    @Test
    @DisplayName("平铺列表接口支持按名称过滤")
    void listSupportsNameFilter() throws Exception {
        mockMvc.perform(get("/api/system/dept/list").header("Authorization", adminToken()).param("deptName", "研发"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].deptId").value(2));
    }

    @Test
    @DisplayName("响应体不包含 delFlag 等内部字段")
    void responseHidesInternalFields() throws Exception {
        mockMvc.perform(get("/api/system/dept/{deptId}", 2).header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ancestors").value("0,1"))
                // delFlag 在实体上标注了 select = false,且响应配置了 non_null,不应出现
                .andExpect(jsonPath("$.data.delFlag").doesNotExist());
    }

    @Test
    @DisplayName("缺少必填字段时返回 400")
    void rejectsMissingRequiredFields() throws Exception {
        DeptForm form = new DeptForm();
        // parentId、deptName、orderNum 均未提供

        mockMvc.perform(post("/api/system/dept")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("手机号格式不合法时返回 400")
    void rejectsInvalidPhone() throws Exception {
        DeptForm form = new DeptForm();
        form.setParentId(1L);
        form.setDeptName("测试部门");
        form.setOrderNum(1);
        form.setPhone("12345");

        mockMvc.perform(post("/api/system/dept")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("手机号")));
    }

    @Test
    @DisplayName("删除仍有下级的部门返回 400 与中文提示")
    void deleteWithChildrenRejected() throws Exception {
        mockMvc.perform(delete("/api/system/dept/{deptId}", 2).header("Authorization", adminToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("该部门下仍存在下级部门,不允许删除"));
    }

    @Test
    @DisplayName("新增部门成功后可通过详情接口查到")
    void addThenQuery() throws Exception {
        DeptForm form = new DeptForm();
        form.setParentId(3L);
        form.setDeptName("渠道组");
        form.setOrderNum(1);

        String body = mockMvc.perform(post("/api/system/dept")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn().getResponse().getContentAsString();

        long newId = objectMapper.readTree(body).get("data").asLong();
        mockMvc.perform(get("/api/system/dept/{deptId}", newId).header("Authorization", adminToken()))
                .andExpect(jsonPath("$.data.deptName").value("渠道组"))
                .andExpect(jsonPath("$.data.ancestors").value("0,1,3"));
    }
}
