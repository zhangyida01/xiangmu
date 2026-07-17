package com.delivery.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.delivery.management.mapper")
public class DeliveryManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryManagementApplication.class, args);
    }
}
