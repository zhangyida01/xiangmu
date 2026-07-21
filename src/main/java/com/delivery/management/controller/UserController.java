package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.User;
import com.delivery.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import com.delivery.management.util.JwtUtil;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户列表（分页）
     */
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
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
        
        IPage<User> result = userService.page(pageParam, wrapper);
        
        // 隐藏密码
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.success(result);
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}

    /**
     * 用户详情
     */
    @GetMapping("/detail/{id
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
        user.setPassword(null);
        return Result.success(user);
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}

    /**
     * 添加用户
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody User user) {
        // 检查用户名是否存在
        if (userService.getByUsername(user.getUsername()) != null) {
            return Result.fail("用户名已存在");
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        boolean success = userService.save(user);
        return success ? Result.success("添加成功") : Result.fail("添加失败");
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}

    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.fail("用户ID不能为空");
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
        
        User existUser = userService.getById(user.getId());
        if (existUser == null) {
            return Result.fail("用户不存在");
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
        
        // 如果修改了密码，需要加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
} else {
            user.setPassword(null); // 不修改密码
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
        
        boolean success = userService.updateById(user);
        return success ? Result.success("更新成功") : Result.fail("更新失败");
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/delete/{id
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}")
    public Result<String> delete(@PathVariable Long id) {
        if (id == 1L) {
            return Result.fail("不能删除管理员账户");
        
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
        
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        
        // 从token中获取用户ID（需要JwtUtil）
        // 这里假设有jwtUtil可以解析token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在");
            }
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("Token无效");
        }
    }
}
