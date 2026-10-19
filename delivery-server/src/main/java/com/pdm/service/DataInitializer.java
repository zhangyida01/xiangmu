package com.pdm.service;

import com.pdm.common.PdmProperties;
import com.pdm.common.Role;
import com.pdm.entity.SysUser;
import com.pdm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 首次启动创建默认管理员，不在 SQL 里写死密码哈希 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PdmProperties props;
    private final AuthService authService;

    @Override
    public void run(ApplicationArguments args) {
        Long count = userMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        String password = props.getInitAdminPassword();
        try {
            authService.checkStrength(password);
        } catch (Exception e) {
            password = "Admin@12345";
            log.warn("PDM_INIT_PASSWORD 不满足密码策略，已使用默认初始密码");
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRealName("管理员");
        admin.setRole(Role.ADMIN);
        admin.setCanViewSecret(true);
        admin.setCanExport(true);
        admin.setCanDownload(true);
        admin.setCanDeleteProject(true);
        admin.setNeedRecheck(false);
        admin.setStatus(1);
        admin.setMustChangePwd(true);
        admin.setLoginFailCount(0);
        admin.setPwdUpdateTime(LocalDateTime.now());
        admin.setRemark("系统初始化创建");
        userMapper.insert(admin);
        log.warn("=========================================================");
        log.warn(" 已创建默认管理员账号 admin，初始密码：{}", password);
        log.warn(" 请立即登录并修改密码（首次登录会强制修改）");
        log.warn("=========================================================");
    }
}