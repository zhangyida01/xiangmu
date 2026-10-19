package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdm.common.BizException;
import com.pdm.common.ErrorCode;
import com.pdm.common.LoginUser;
import com.pdm.common.Perm;
import com.pdm.common.Role;
import com.pdm.common.UserContext;
import com.pdm.common.WebUtils;
import com.pdm.dto.LoginRequest;
import com.pdm.dto.LoginResponse;
import com.pdm.entity.SysUser;
import com.pdm.mapper.SysUserMapper;
import com.pdm.security.CaptchaService;
import com.pdm.security.JwtService;
import com.pdm.security.TokenStore;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST = "blacklist:";
    private static final String RECHECK = "recheck:";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenStore tokenStore;
    private final CaptchaService captchaService;
    private final ConfigService configService;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest req, HttpServletRequest http) {
        captchaService.verify(req.getCaptchaId(), req.getCaptcha());
        String ip = WebUtils.clientIp(http);

        SysUser user = findByUsername(req.getUsername());
        if (user == null) {
            auditService.record("AUTH", "LOGIN", "user", null, req.getUsername(), null, "账号不存在", false, "账号或密码错误");
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号已禁用");
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            long mins = Duration.between(LocalDateTime.now(), user.getLockUntil()).toMinutes() + 1;
            throw new BizException(ErrorCode.LOCKED, "账号已锁定，请 " + mins + " 分钟后重试");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            onLoginFail(user);
            auditService.record("AUTH", "LOGIN", "user", user.getId(), user.getUsername(), null,
                    "密码错误，失败次数 " + user.getLoginFailCount(), false, "账号或密码错误");
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        user.setLoginFailCount(0);
        user.setLockUntil(null);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        String token = jwtService.create(user.getId(), user.getUsername(), user.getRole());
        auditService.record("AUTH", "LOGIN", "user", user.getId(), user.getUsername(), null, "登录成功", true, null);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setExpiresIn(jwtService.expireHours() * 3600L);
        resp.setUser(toUserInfo(user));
        return resp;
    }

    private void onLoginFail(SysUser user) {
        int fails = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
        user.setLoginFailCount(fails);
        int limit = configService.loginFailLimit();
        if (fails >= limit) {
            user.setLockUntil(LocalDateTime.now().plusMinutes(configService.loginLockMinutes()));
            user.setLoginFailCount(0);
        }
        userMapper.updateById(user);
    }

    /** 拦截器调用：校验 Token 并装配当前登录用户 */
    public LoginUser authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录或登录已失效");
        }
        Claims claims = jwtService.parse(token);
        String jti = claims.getId();
        if (jti != null && tokenStore.contains(BLACKLIST + jti)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        Long uid;
        try {
            uid = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        SysUser user = userMapper.selectById(uid);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号不存在或已禁用");
        }
        LoginUser lu = new LoginUser();
        lu.setId(user.getId());
        lu.setUsername(user.getUsername());
        lu.setRealName(user.getRealName());
        lu.setRole(user.getRole());
        lu.setPerms(permsOf(user));
        lu.setNeedRecheck(Boolean.TRUE.equals(user.getNeedRecheck()));
        lu.setMustChangePwd(Boolean.TRUE.equals(user.getMustChangePwd()));
        return lu;
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims claims = jwtService.parse(token);
            long remain = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
            if (claims.getId() != null && remain > 0) {
                tokenStore.put(BLACKLIST + claims.getId(), "1", remain);
            }
            LoginUser u = UserContext.get();
            auditService.record("AUTH", "LOGOUT", "user", u == null ? null : u.getId(),
                    u == null ? null : u.getUsername(), null, null, true, null);
        } catch (BizException ignored) {
            // Token 已失效，无需处理
        }
    }

    public LoginResponse.UserInfo me() {
        LoginUser lu = UserContext.require();
        SysUser user = userMapper.selectById(lu.getId());
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号不存在");
        }
        return toUserInfo(user);
    }

    public void changePassword(String oldPassword, String newPassword) {
        LoginUser lu = UserContext.require();
        SysUser user = userMapper.selectById(lu.getId());
        if (user == null) {
            throw BizException.notFound("用户");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException(ErrorCode.RECHECK_FAILED, "原密码不正确");
        }
        if (oldPassword.equals(newPassword)) {
            throw BizException.badParam("新密码不能与原密码相同");
        }
        checkStrength(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPwdUpdateTime(LocalDateTime.now());
        user.setMustChangePwd(false);
        userMapper.updateById(user);
        auditService.record("AUTH", "CHANGE_PASSWORD", "user", user.getId(), user.getUsername(), null, null, true, null);
    }

    /** 二次密码校验，返回 5 分钟内有效的 recheckToken */
    public String recheck(String password) {
        LoginUser lu = UserContext.require();
        SysUser user = userMapper.selectById(lu.getId());
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            auditService.record("AUTH", "RECHECK", "user", lu.getId(), lu.getUsername(), null, "二次校验失败", false, "密码错误");
            throw new BizException(ErrorCode.RECHECK_FAILED, "密码校验失败");
        }
        String t = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(RECHECK + t, String.valueOf(lu.getId()), 300);
        return t;
    }

    public boolean verifyRecheckToken(String recheckToken) {
        if (!configService.getBool("cred.recheck.enabled", false)) {
            return true;
        }
        if (recheckToken == null || recheckToken.isBlank()) {
            return false;
        }
        String owner = tokenStore.get(RECHECK + recheckToken);
        return owner != null && owner.equals(String.valueOf(UserContext.require().getId()));
    }

    public void checkStrength(String pwd) {
        if (pwd == null || pwd.length() < 8) {
            throw BizException.badParam("密码至少 8 位");
        }
        boolean hasLetter = pwd.chars().anyMatch(Character::isLetter);
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw BizException.badParam("密码需同时包含字母和数字");
        }
    }

    public static Set<String> permsOf(SysUser u) {
        Set<String> s = new HashSet<>();
        if (Role.ADMIN.equals(u.getRole())) {
            s.add(Perm.SECRET_VIEW);
            s.add(Perm.SECRET_EXPORT);
            s.add(Perm.FILE_DOWNLOAD);
            s.add(Perm.PROJECT_DELETE);
            s.add(Perm.SYSTEM_MANAGE);
            s.add(Perm.AUDIT_VIEW);
            return s;
        }
        if (Role.VIEWER.equals(u.getRole())) {
            return s;
        }
        if (Boolean.TRUE.equals(u.getCanViewSecret())) {
            s.add(Perm.SECRET_VIEW);
        }
        if (Boolean.TRUE.equals(u.getCanExport())) {
            s.add(Perm.SECRET_EXPORT);
        }
        if (Boolean.TRUE.equals(u.getCanDownload())) {
            s.add(Perm.FILE_DOWNLOAD);
        }
        if (Boolean.TRUE.equals(u.getCanDeleteProject())) {
            s.add(Perm.PROJECT_DELETE);
        }
        return s;
    }

    private LoginResponse.UserInfo toUserInfo(SysUser u) {
        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setId(u.getId());
        info.setUsername(u.getUsername());
        info.setRealName(u.getRealName());
        info.setRole(u.getRole());
        info.setMustChangePwd(Boolean.TRUE.equals(u.getMustChangePwd()));
        info.setPermissions(new ArrayList<>(permsOf(u)));
        return info;
    }

    public SysUser findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }
}