package com.pdm.dto;

import lombok.Data;

@Data
public class RevealResponse {

    private Long id;
    private String username;
    private String secret;
    private String secretType;
    private int autoHideSec;
    private int clipboardClearSec;
    private Long accessId;
}