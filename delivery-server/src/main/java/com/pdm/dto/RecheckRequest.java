package com.pdm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecheckRequest {

    @NotBlank(message = "密码不能为空")
    private String password;
}