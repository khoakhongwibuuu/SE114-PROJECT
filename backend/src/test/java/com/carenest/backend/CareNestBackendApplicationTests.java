package com.carenest.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: Kiểm tra Spring ApplicationContext khởi tạo thành công.
 */
@SpringBootTest
class CareNestBackendApplicationTests {

    @Test
    void contextLoads() {
        // Nếu test này pass → Spring Boot đã khởi tạo đúng tất cả Bean
    }
}
