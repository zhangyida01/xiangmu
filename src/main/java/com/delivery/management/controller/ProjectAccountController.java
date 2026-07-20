package com.delivery.management.controller;

import com.delivery.management.common.Result;
import com.delivery.management.entity.ProjectAccount;
import com.delivery.management.service.ProjectAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project-account")
public class ProjectAccountController {

    @Autowired
    private ProjectAccountService projectAccountService;

    @GetMapping("/list/{projectId}")
    public Result<List<ProjectAccount>> listByProject(@PathVariable Long projectId) {
        List<ProjectAccount> accounts = projectAccountService.getByProjectId(projectId);
        return Result.success(accounts);
    }

    @GetMapping("/detail/{id}")
    public Result<ProjectAccount> detail(@PathVariable Long id) {
        ProjectAccount account = projectAccountService.getById(id);
        if (account == null) {
            return Result.fail("Account not found");
        }
        return Result.success(account);
    }
    
    @GetMapping("/password/{id}")
    public Result<String> getPassword(@PathVariable Long id) {
        ProjectAccount account = projectAccountService.getById(id);
        if (account == null) {
            return Result.fail("Account not found");
        }
        return Result.success(account.getPassword());
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ProjectAccount account) {
        boolean success = projectAccountService.save(account);
        return success ? Result.success("Add success") : Result.fail("Add failed");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ProjectAccount account) {
        if (account.getId() == null) {
            return Result.fail("Account ID cannot be null");
        }
        
        boolean success = projectAccountService.updateById(account);
        return success ? Result.success("Update success") : Result.fail("Update failed");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = projectAccountService.removeById(id);
        return success ? Result.success("Delete success") : Result.fail("Delete failed");
    }
}
