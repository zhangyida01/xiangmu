package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.common.PdmProperties;
import com.pdm.common.RequiresPerm;
import com.pdm.common.Role;
import com.pdm.entity.SysConfig;
import com.pdm.entity.SysDictItem;
import com.pdm.service.ConfigService;
import com.pdm.service.DictService;
import com.pdm.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SystemController {

    private final ConfigService configService;
    private final DictService dictService;
    private final FileStorage fileStorage;
    private final PdmProperties props;
    private final com.pdm.mapper.StatsMapper statsMapper;

    @GetMapping("/api/system/health")
    public Map<String, Object> health() {
        Map<String, Object> m = new HashMap<>();
        boolean db = true;
        try {
            statsMapper.projectCount();
        } catch (Exception e) {
            db = false;
        }
        m.put("status", db ? "UP" : "DOWN");
        m.put("db", db);
        return m;
    }

    @GetMapping("/api/system/info")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Map<String, Object>> info() {
        Map<String, Object> m = new HashMap<>();
        m.put("app", "delivery-server");
        m.put("systemName", configService.getString("sys.name", "项目交付管理后台"));
        m.put("version", "1.0.0");
        m.put("javaVersion", System.getProperty("java.version"));
        m.put("uploadDir", props.getUploadDir());
        m.put("storageUsedBytes", fileStorage.totalSize());
        m.put("freeDiskBytes", java.io.File.listRoots().length > 0 ? java.io.File.listRoots()[0].getFreeSpace() : -1);
        return ApiResult.ok(m);
    }

    @GetMapping("/api/configs")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<List<SysConfig>> configs(@RequestParam(required = false) String group) {
        return ApiResult.ok(configService.list(group));
    }

    @PutMapping("/api/configs")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> saveConfigs(@RequestBody Map<String, String> values) {
        configService.save(values);
        return ApiResult.ok();
    }

    @GetMapping("/api/dicts/items")
    public ApiResult<Map<String, List<SysDictItem>>> dictItems(@RequestParam String types) {
        return ApiResult.ok(dictService.byTypes(List.of(types.split(","))));
    }

    @GetMapping("/api/dicts/all")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<List<SysDictItem>> dictAll() {
        return ApiResult.ok(dictService.all());
    }

    @PostMapping("/api/dicts/items")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Long> createDict(@RequestBody SysDictItem item) {
        return ApiResult.ok(dictService.create(item));
    }

    @PutMapping("/api/dicts/items/{id}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> updateDict(@PathVariable Long id, @RequestBody SysDictItem item) {
        dictService.update(id, item);
        return ApiResult.ok();
    }

    @DeleteMapping("/api/dicts/items/{id}")
    @RequiresPerm(roles = {Role.ADMIN})
    public ApiResult<Void> deleteDict(@PathVariable Long id) {
        dictService.delete(id);
        return ApiResult.ok();
    }
}