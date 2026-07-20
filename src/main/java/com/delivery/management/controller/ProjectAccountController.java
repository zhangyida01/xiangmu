package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.ProjectAccount;
import com.delivery.management.service.ProjectAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project-account")
public class ProjectAccountController {

    @Autowired
    private ProjectAccountService projectAccountService;

    @GetMapping("/list")
    public Result<Page<ProjectAccount>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ProjectAccount> page = new Page<>(current, size);
        QueryWrapper<ProjectAccount> wrapper = new QueryWrapper<>();
        if (projectId != null) {
            wrapper.eq("project_id", projectId);
        }
        Page<ProjectAccount> result = projectAccountService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ProjectAccount> detail(@PathVariable Long id) {
        ProjectAccount account = projectAccountService.getById(id);
        if (account == null) {
            return Result.fail("账户不存在");
        }
        return Result.success(account);
    }

    @PostMapping
    public Result<String> add(@RequestBody ProjectAccount account) {
        boolean success = projectAccountService.save(account);
        return success ? Result.success("添加成功") : Result.fail("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody ProjectAccount account) {
        if (account.getId() == null) {
            return Result.fail("账户ID不能为空");
        }
        boolean success = projectAccountService.updateById(account);
        return success ? Result.success("修改成功") : Result.fail("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = projectAccountService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
}