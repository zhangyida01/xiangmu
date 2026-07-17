package com.delivery.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.delivery.management.dto.LoginRequest;
import com.delivery.management.dto.LoginResponse;
import com.delivery.management.entity.User;

public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
}
