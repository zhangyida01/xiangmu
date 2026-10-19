package com.pdm.common;

import jakarta.servlet.http.HttpServletRequest;

public final class WebUtils {

    private WebUtils() {
    }

    public static String clientIp(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        String ip = req.getHeader("X-Forwarded-For");
        if (notEmpty(ip)) {
            int idx = ip.indexOf(',');
            return (idx > 0 ? ip.substring(0, idx) : ip).trim();
        }
        ip = req.getHeader("X-Real-IP");
        if (notEmpty(ip)) {
            return ip.trim();
        }
        return req.getRemoteAddr();
    }

    public static String userAgent(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        String ua = req.getHeader("User-Agent");
        if (ua == null) {
            return null;
        }
        return ua.length() > 480 ? ua.substring(0, 480) : ua;
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isBlank() && !"unknown".equalsIgnoreCase(s);
    }
}