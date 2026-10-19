package com.pdm.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private boolean mustChangePwd;
        private List<String> permissions;
    }
}