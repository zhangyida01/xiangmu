package com.pdm.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        LoginUser user;
        try {
            user = authService.authenticate(resolveToken(request));
        } catch (BizException e) {
            write(response, e.getCode(), e.getMessage());
            return false;
        }
        UserContext.set(user);

        RequiresPerm rp = hm.getMethodAnnotation(RequiresPerm.class);
        if (rp == null) {
            rp = hm.getBeanType().getAnnotation(RequiresPerm.class);
        }
        if (rp != null && !allowed(user, rp)) {
            write(response, ErrorCode.FORBIDDEN, "无权限执行该操作");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean allowed(LoginUser user, RequiresPerm rp) {
        if (user.isAdmin()) {
            return true;
        }
        if (rp.roles().length > 0) {
            boolean hit = false;
            for (String r : rp.roles()) {
                if (r.equals(user.getRole())) {
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                return false;
            }
        }
        for (String p : rp.perms()) {
            if (!user.hasPerm(p)) {
                return false;
            }
        }
        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String h = request.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            return h.substring(7).trim();
        }
        String p = request.getParameter("token");
        return p == null || p.isBlank() ? null : p;
    }

    private void write(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.fail(code, msg)));
    }
}