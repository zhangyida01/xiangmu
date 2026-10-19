package com.pdm.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpireAlert {

    private String srcType;
    private Long srcId;
    private Long projectId;
    private String projectName;
    private String itemName;
    private LocalDate expireDate;
    private Integer daysLeft;
}