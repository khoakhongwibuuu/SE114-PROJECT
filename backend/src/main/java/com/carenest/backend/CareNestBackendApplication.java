package com.carenest.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CareNest Backend - Entry Point
 *
 * Ứng dụng quản lý sức khỏe gia đình.
 * - @SpringBootApplication: Bật auto-config, component scan, configuration
 * - @EnableScheduling: Cho phép chạy Cronjob (@Scheduled) nhắc thuốc, lịch khám
 */
@SpringBootApplication
@EnableScheduling
public class CareNestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareNestBackendApplication.class, args);
    }
}
