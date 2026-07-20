package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.Customer;
import com.delivery.management.entity.Project;
import com.delivery.management.entity.User;
import com.delivery.management.service.CustomerService;
import com.delivery.management.service.ProjectService;
import com.delivery.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    public Result<IPage<Project>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Page<Project> pageParam = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Project::getProjectName, keyword)
                   .or()
                   .like(Project::getProjectCode, keyword);
        }
        
        wrapper.orderByDesc(Project::getCreateTime);
        
        IPage<Project> result = projectService.page(pageParam, wrapper);
        
        // Fill manager name and customer name
        result.getRecords().forEach(project -> {
            if (project.getProjectManagerId() != null) {
                User manager = userService.getById(project.getProjectManagerId());
                if (manager != null) {
                    project.setManagerName(manager.getRealName() != null ? manager.getRealName() : manager.getUsername());
                }
            }
            if (project.getCustomerId() != null) {
                Customer customer = customerService.getById(project.getCustomerId());
                if (customer != null) {
                    project.setCustomerName(customer.getCompanyName());
                }
            }
        });
        
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<Project> detail(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.fail("项目不存在");
        }
        return Result.success(project);
    }

    @PostMapping
    public Result<String> add(@RequestBody Project project) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getProjectCode, project.getProjectCode());
        if (projectService.count(wrapper) > 0) {
            return Result.fail("项目编号已存在");
        }
        
        boolean success = projectService.save(project);
        return success ? Result.success("创建成功") : Result.fail("创建失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Project project) {
        if (project.getId() == null) {
            return Result.fail("项目ID不能为空");
        }
        
        boolean success = projectService.updateById(project);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = projectService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
}