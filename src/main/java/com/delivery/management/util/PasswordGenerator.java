package com.delivery.management.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encoded = encoder.encode(password);
        
        System.out.println("================================");
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密: " + encoded);
        System.out.println("================================");
        System.out.println("SQL语句:");
        System.out.println("UPDATE sys_user SET password = '" + encoded + "' WHERE username = 'admin';");
        System.out.println("================================");
        
        // 验证
        boolean matches = encoder.matches(password, encoded);
        System.out.println("验证结果: " + (matches ? "成功" : "失败"));
    }
}
