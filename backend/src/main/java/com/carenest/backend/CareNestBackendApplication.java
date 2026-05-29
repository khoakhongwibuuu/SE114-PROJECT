package com.carenest.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CareNest Backend - Entry Point
 *
 * á»¨ng dá»¥ng quáº£n lÃ½ sá»©c khá»e gia Ä‘Ã¬nh.
 * - @SpringBootApplication: Báº­t auto-config, component scan, configuration
 * - @EnableScheduling: Cho phÃ©p cháº¡y Cronjob (@Scheduled) nháº¯c thuá»‘c, lá»‹ch khÃ¡m
 */
@SpringBootApplication(excludeName = {"org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration"})
@EnableScheduling
@EnableCaching
public class CareNestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareNestBackendApplication.class, args);
    }
}
