package com.pdm.common;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录或登录已失效");
        }
        return u;
    }

    public static Long userId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getId();
    }

    public static String username() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUsername();
    }

    public static void clear() {
        HOLDER.remove();
    }
}