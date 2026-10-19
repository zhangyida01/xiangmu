package com.pdm.dto;

import lombok.Data;

@Data
public class AlertSummary {

    private String srcType;
    private Long cnt;
    private Integer minDays;
}