package com.delivery.management.controller;

import com.delivery.management.common.Result;
import com.delivery.management.entity.User;
import com.delivery.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/init")
public class InitController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 初始化管理员账户
     * 只能调用一次，如果admin已存在会重置密码
     */
    @PostMapping("/admin")
    public Result<String> initAdmin() {
        try {
            // 查找admin用户
            User admin = userService.getByUsername("admin");
            
            if (admin == null) {
                // 创建新管理员
                admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRealName("系统管理员");
                admin.setStatus(1);
                userService.save(admin);
                return Result.success("管理员账户创建成功！用户名：admin，密码：admin123");
            } else {
                // 重置密码
                admin.setPassword(passwordEncoder.encode("admin123"));
                userService.updateById(admin);
                return Result.success("管理员密码已重置为：admin123");
            }
        } catch (Exception e) {
            return Result.fail("初始化失败：" + e.getMessage());
        }
    }
}
