package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.Project;
import com.delivery.management.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 项目列表（分页）
     */
    @GetMapping("/list")
    public Result<IPage<Project>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        
        Page<Project> pageParam = new Page<>(page, size);
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
        return Result.success(result);
    }

    /**
     * 项目详情
     */
    @GetMapping("/detail/{id}")
    public Result<Project> detail(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.fail("项目不存在");
        }
        return Result.success(project);
    }

    /**
     * 创建项目
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Project project) {
        // 检查项目编号是否存在
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Project::getProjectCode, project.getProjectCode());
        if (projectService.count(wrapper) > 0) {
            return Result.fail("项目编号已存在");
        }
        
        boolean success = projectService.save(project);
        return success ? Result.success("创建成功") : Result.fail("创建失败");
    }

    /**
     * 更新项目
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Project project) {
        if (project.getId() == null) {
            return Result.fail("项目ID不能为空");
        }
        
        boolean success = projectService.updateById(project);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = projectService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
}
