package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.common.Audit;
import com.pdm.common.PageResult;
import com.pdm.common.RequiresPerm;
import com.pdm.common.Role;
import com.pdm.dto.UserForm;
import com.pdm.entity.SysUser;
import com.pdm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@RequiresPerm(roles = {Role.ADMIN})
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResult<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String role,
                                              @RequestParam(required = false) Integer status) {
        return ApiResult.ok(userService.page(page, size, keyword, role, status));
    }

    @GetMapping("/{id}")
    public ApiResult<SysUser> get(@PathVariable Long id) {
        return ApiResult.ok(userService.get(id));
    }

    @PostMapping
    @Audit(module = "SYSTEM", action = "CREATE_USER", targetType = "user", recordArgs = false)
    public ApiResult<Long> create(@Valid @RequestBody UserForm form) {
        return ApiResult.ok(userService.create(form));
    }

    @PutMapping("/{id}")
    @Audit(module = "SYSTEM", action = "UPDATE_USER", targetType = "user", recordArgs = false)
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody UserForm form) {
        userService.update(id, form);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/password/reset")
    public ApiResult<Map<String, String>> resetPassword(@PathVariable Long id) {
        return ApiResult.ok("密码已重置", Map.of("password", userService.resetPassword(id)));
    }

    @PutMapping("/{id}/unlock")
    public ApiResult<Void> unlock(@PathVariable Long id) {
        userService.unlock(id);
        return ApiResult.ok();
    }
}