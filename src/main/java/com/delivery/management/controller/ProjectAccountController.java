package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.ProjectAccount;
import com.delivery.management.entity.User;
import com.delivery.management.service.ProjectAccountService;
import com.delivery.management.service.UserService;
import com.delivery.management.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/project-account")
public class ProjectAccountController {

    @Autowired
    private ProjectAccountService projectAccountService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

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
        wrapper.orderByDesc("create_time");
        Page<ProjectAccount> result = projectAccountService.page(page, wrapper);
        
        // Fill creator name
        result.getRecords().forEach(account -> {
            if (account.getCreatorId() != null) {
                User creator = userService.getById(account.getCreatorId());
                if (creator != null) {
                    account.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
                }
            }
        });
        
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
    public Result<String> add(@RequestBody ProjectAccount account, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            account.setCreatorId(userId);
        }
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