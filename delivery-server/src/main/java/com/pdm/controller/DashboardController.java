package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.dto.AlertSummary;
import com.pdm.dto.ExpireAlert;
import com.pdm.service.AlertService;
import com.pdm.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final AlertService alertService;
    private final StatsService statsService;

    @GetMapping("/alerts/expiring")
    public ApiResult<List<ExpireAlert>> expiring(@RequestParam(required = false) Integer days) {
        return ApiResult.ok(alertService.expiring(days));
    }

    @GetMapping("/alerts/summary")
    public ApiResult<List<AlertSummary>> summary(@RequestParam(required = false) Integer days) {
        return ApiResult.ok(alertService.summary(days));
    }

    @GetMapping("/stats/dashboard")
    public ApiResult<Map<String, Object>> dashboard() {
        return ApiResult.ok(statsService.dashboard());
    }
}