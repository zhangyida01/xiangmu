package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdm.entity.SysConfig;
import com.pdm.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统参数读取，带内存缓存。
 * 首次加载放在 ApplicationReadyEvent 而不是 @PostConstruct：
 * MyBatis 的 Mapper 不受 Spring Boot 数据库初始化顺序约束，
 * 在 Bean 初始化阶段查库可能早于 Flyway 建表。读取时若尚未加载会自动补一次。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {

    private final SysConfigMapper configMapper;
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private volatile boolean loaded = false;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        reload();
    }

    public synchronized void reload() {
        try {
            Map<String, String> fresh = new HashMap<>();
            for (SysConfig c : configMapper.selectList(null)) {
                if (c.getCfgKey() != null) {
                    fresh.put(c.getCfgKey(), c.getCfgValue());
                }
            }
            cache.clear();
            cache.putAll(fresh);
            loaded = true;
            log.info("系统参数已加载 {} 项", cache.size());
        } catch (Exception e) {
            loaded = false;
            log.warn("系统参数加载失败，将在下次读取时重试：{}", e.getMessage());
        }
    }

    private void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    public String getString(String key, String def) {
        ensureLoaded();
        String v = cache.get(key);
        return v == null || v.isBlank() ? def : v;
    }

    public int getInt(String key, int def) {
        try {
            return Integer.parseInt(getString(key, String.valueOf(def)).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public boolean getBool(String key, boolean def) {
        String v = getString(key, String.valueOf(def));
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }

    public List<SysConfig> list(String group) {
        LambdaQueryWrapper<SysConfig> w = new LambdaQueryWrapper<>();
        w.eq(group != null && !group.isBlank(), SysConfig::getCfgGroup, group)
                .orderByAsc(SysConfig::getCfgGroup).orderByAsc(SysConfig::getId);
        return configMapper.selectList(w);
    }

    public void save(Map<String, String> values) {
        values.forEach((k, v) -> {
            SysConfig exist = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getCfgKey, k));
            if (exist == null) {
                SysConfig c = new SysConfig();
                c.setCfgKey(k);
                c.setCfgValue(v);
                configMapper.insert(c);
            } else {
                exist.setCfgValue(v);
                configMapper.updateById(exist);
            }
            cache.put(k, v);
        });
        loaded = true;
    }

    public int recheckEnabled() {
        return getBool("cred.recheck.enabled", false) ? 1 : 0;
    }

    public int clipboardClearSec() {
        return getInt("cred.clipboard.clearSec", 30);
    }

    public int plainHideSec() {
        return getInt("cred.plain.hideSec", 60);
    }

    public int alertDays() {
        return getInt("alert.days", 30);
    }

    public int loginFailLimit() {
        return getInt("login.failLimit", 5);
    }

    public int loginLockMinutes() {
        return getInt("login.lockMinutes", 15);
    }

    public int defaultRotateDays() {
        return getInt("cred.defaultRotateDays", 180);
    }

    /** 文档上传单文件上限（字节），取自 upload.maxSizeMb */
    public long uploadMaxBytes() {
        return getInt("upload.maxSizeMb", 500) * 1024L * 1024L;
    }

    /** 凭据加密附件上限（字节），取自 cred.attachment.maxSizeMb；附件需整体读入内存加密，默认给小值 */
    public long credAttachmentMaxBytes() {
        return getInt("cred.attachment.maxSizeMb", 20) * 1024L * 1024L;
    }

    public List<String> allowedExtensions() {
        String v = getString("upload.allowExt", "pdf,doc,docx,xls,xlsx,ppt,pptx,txt,md,png,jpg,jpeg,zip");
        return List.of(v.split(","));
    }
}