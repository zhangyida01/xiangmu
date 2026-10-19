package com.pdm.service;

import com.pdm.dto.AlertSummary;
import com.pdm.dto.ExpireAlert;
import com.pdm.mapper.AlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertMapper alertMapper;
    private final ConfigService configService;

    public List<ExpireAlert> expiring(Integer days) {
        return alertMapper.listExpiring(days == null ? configService.alertDays() : days);
    }

    public List<AlertSummary> summary(Integer days) {
        return alertMapper.summary(days == null ? configService.alertDays() : days);
    }
}