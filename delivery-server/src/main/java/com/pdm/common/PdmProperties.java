package com.pdm.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pdm")
public class PdmProperties {

    private String masterKeyFile = "./data/keys/master.key";
    private String uploadDir = "./data/uploads";
    private String jwtSecret = "dev-only-secret-key-please-change-in-prod-0123456789";
    private long jwtExpireHours = 12;
    private String initAdminPassword = "Admin@12345";
    private boolean captchaEnabled = true;
}