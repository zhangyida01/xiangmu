package com.pdm.service;

import com.pdm.mapper.StatsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StatsMapper statsMapper;
    private final ConfigService configService;

    public Map<String, Object> dashboard() {
        Map<String, Object> m = new HashMap<>();
        m.put("projectCount", statsMapper.projectCount());
        m.put("activeProjectCount", statsMapper.activeProjectCount());
        m.put("customerCount", statsMapper.customerCount());
        m.put("documentCount", statsMapper.documentCount());
        m.put("credentialCount", statsMapper.credentialCount());
        m.put("acceptingIn30Days", statsMapper.acceptingIn30Days());
        m.put("alertCount", statsMapper.alertCount(configService.alertDays()));
        m.put("avgDocRate", statsMapper.avgDocRate());
        m.put("statusGroup", statsMapper.projectStatusGroup());
        m.put("typeGroup", statsMapper.projectTypeGroup());
        m.put("recentProjects", statsMapper.recentProjects(10));
        return m;
    }

    public List<Map<String, Object>> statusGroup() {
        return statsMapper.projectStatusGroup();
    }
}