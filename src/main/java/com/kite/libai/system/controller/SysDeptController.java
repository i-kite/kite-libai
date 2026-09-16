package com.kite.libai.system.controller;

import com.kite.libai.common.core.domain.R;
import com.kite.libai.system.domain.SysDept;
import com.kite.libai.system.domain.dto.DeptForm;
import com.kite.libai.system.service.ISysDeptService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 部门管理。
 *
 * @author kite
 */
@RestController
@RequestMapping(path = "/api/system/dept", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysDeptController {

    private final ISysDeptService sysDeptService;

    @Autowired
    public SysDeptController(ISysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    /**
     * 部门列表(平铺)。
     */
    @GetMapping("/list")
    public R<List<SysDept>> list(@RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(sysDeptService.selectDeptList(deptName, status));
    }

    /**
     * 部门树。
     */
    @GetMapping("/tree")
    public R<List<SysDept>> tree(@RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(sysDeptService.selectDeptTree(deptName, status));
    }

    /**
     * 部门详情。
     */
    @GetMapping("/{deptId}")
    public R<SysDept> detail(@PathVariable Long deptId) {
        return R.ok(sysDeptService.selectDeptById(deptId));
    }

    /**
     * 新增部门。
     */
    @PostMapping
    public R<Long> add(@RequestBody @Valid DeptForm form) {
        return R.ok("新增成功", sysDeptService.insertDept(form));
    }

    /**
     * 修改部门。
     */
    @PutMapping
    public R<Void> edit(@RequestBody @Valid DeptForm form) {
        sysDeptService.updateDept(form);
        return R.ok("修改成功", null);
    }

    /**
     * 删除部门。
     */
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        sysDeptService.deleteDept(deptId);
        return R.ok("删除成功", null);
    }
}
