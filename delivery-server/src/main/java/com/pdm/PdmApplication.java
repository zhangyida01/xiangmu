package com.pdm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.pdm.mapper")
public class PdmApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdmApplication.class, args);
    }
}