package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pdm.common.BizException;
import com.pdm.common.PageResult;
import com.pdm.common.Role;
import com.pdm.common.UserContext;
import com.pdm.dto.UserForm;
import com.pdm.entity.SysUser;
import com.pdm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String PWD_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RND = new SecureRandom();

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final AuditService auditService;

    public PageResult<SysUser> page(long page, long size, String keyword, String role, Integer status) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        w.eq(role != null && !role.isBlank(), SysUser::getRole, role)
                .eq(status != null, SysUser::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), q -> q
                        .like(SysUser::getUsername, keyword).or()
                        .like(SysUser::getRealName, keyword))
                .orderByAsc(SysUser::getId);
        return PageResult.of(userMapper.selectPage(new Page<>(page, size), w));
    }

    public SysUser get(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) {
            throw BizException.notFound("用户");
        }
        return u;
    }

    public Long create(UserForm form) {
        if (authService.findByUsername(form.getUsername()) != null) {
            throw BizException.conflict("账号已存在：" + form.getUsername());
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw BizException.badParam("请设置初始密码");
        }
        authService.checkStrength(form.getPassword());
        SysUser u = new SysUser();
        BeanUtils.copyProperties(form, u, "id", "password");
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.setMustChangePwd(true);
        u.setPwdUpdateTime(LocalDateTime.now());
        u.setLoginFailCount(0);
        applyDefaults(u);
        userMapper.insert(u);
        auditService.record("SYSTEM", "CREATE", "user", u.getId(), u.getUsername(), null, "新建用户", true, null);
        return u.getId();
    }

    public void update(Long id, UserForm form) {
        SysUser u = get(id);
        if (!u.getUsername().equals(form.getUsername()) && authService.findByUsername(form.getUsername()) != null) {
            throw BizException.conflict("账号已存在：" + form.getUsername());
        }
        guardLastAdmin(u, form);
        BeanUtils.copyProperties(form, u, "id", "password", "createTime", "updateTime", "deleted");
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            authService.checkStrength(form.getPassword());
            u.setPassword(passwordEncoder.encode(form.getPassword()));
            u.setPwdUpdateTime(LocalDateTime.now());
            u.setMustChangePwd(true);
        }
        applyDefaults(u);
        userMapper.updateById(u);
        auditService.record("SYSTEM", "UPDATE", "user", id, u.getUsername(), null, "修改用户", true, null);
    }

    public void delete(Long id) {
        SysUser u = get(id);
        if (id.equals(UserContext.userId())) {
            throw BizException.conflict("不能删除当前登录账号");
        }
        if (Role.ADMIN.equals(u.getRole()) && countAdmins() <= 1) {
            throw BizException.conflict("必须保留至少一个管理员");
        }
        userMapper.deleteById(id);
        auditService.record("SYSTEM", "DELETE", "user", id, u.getUsername(), null, "删除用户", true, null);
    }

    public String resetPassword(Long id) {
        SysUser u = get(id);
        String pwd = randomPassword();
        u.setPassword(passwordEncoder.encode(pwd));
        u.setMustChangePwd(true);
        u.setPwdUpdateTime(LocalDateTime.now());
        u.setLoginFailCount(0);
        u.setLockUntil(null);
        userMapper.updateById(u);
        auditService.record("SYSTEM", "RESET_PASSWORD", "user", id, u.getUsername(), null, null, true, null);
        return pwd;
    }

    public void unlock(Long id) {
        SysUser u = get(id);
        u.setLockUntil(null);
        u.setLoginFailCount(0);
        userMapper.updateById(u);
    }

    private void guardLastAdmin(SysUser origin, UserForm form) {
        boolean wasAdmin = Role.ADMIN.equals(origin.getRole());
        boolean stillAdmin = form.getRole() == null || Role.ADMIN.equals(form.getRole());
        boolean disabled = form.getStatus() != null && form.getStatus() == 0;
        if ((wasAdmin && (!stillAdmin || disabled)) && countAdmins() <= 1) {
            throw BizException.conflict("必须保留至少一个可用的管理员");
        }
    }

    private long countAdmins() {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, Role.ADMIN).eq(SysUser::getStatus, 1));
    }

    private void applyDefaults(SysUser u) {
        if (u.getRole() == null) {
            u.setRole(Role.USER);
        }
        if (u.getStatus() == null) {
            u.setStatus(1);
        }
        if (Role.ADMIN.equals(u.getRole())) {
            u.setCanViewSecret(true);
            u.setCanExport(true);
            u.setCanDownload(true);
            u.setCanDeleteProject(true);
        }
        if (Role.VIEWER.equals(u.getRole())) {
            u.setCanViewSecret(false);
            u.setCanExport(false);
            u.setCanDownload(false);
            u.setCanDeleteProject(false);
        }
        if (u.getCanDownload() == null) {
            u.setCanDownload(true);
        }
    }

    private String randomPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(PWD_POOL.charAt(RND.nextInt(PWD_POOL.length())));
        }
        return sb.insert(8, '@').toString();
    }
}