package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.User;
import com.delivery.management.service.UserService;
import com.delivery.management.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getRealName, keyword)
                   .or()
                   .like(User::getEmail, keyword);
        }
        
        IPage<User> result = userService.page(pageParam, wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    }

    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }

    @PostMapping
    public Result<String> add(@RequestBody User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        boolean success = userService.save(user);
        return success ? Result.success("添加成功") : Result.fail("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            User existUser = userService.getById(user.getId());
            user.setPassword(existUser.getPassword());
        }
        boolean success = userService.updateById(user);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
}