package com.pdm.common;

import lombok.Data;

import java.util.Set;

@Data
public class LoginUser {

    private Long id;
    private String username;
    private String realName;
    private String role;
    private Set<String> perms;
    private boolean needRecheck;
    private boolean mustChangePwd;

    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }

    public boolean hasPerm(String perm) {
        return isAdmin() || (perms != null && perms.contains(perm));
    }

    public boolean hasRole(String r) {
        return isAdmin() || r.equals(role);
    }
}