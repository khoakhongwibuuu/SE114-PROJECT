package com.carenest.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CareNest RESTful API",
                version = "1.0",
                description = "Tài liệu API cho ứng dụng quản lý sức khỏe gia đình CareNest.",
                contact = @Contact(
                        name = "Tuấn Kiệt & Anh Khoa",
                        email = "contact@carenest.com",
                        url = "https://github.com/khoakhongwibuuu/SE114-PROJECT"
                )
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Nhập JWT Token tại đây để truy cập các API bị khóa bảo mật (có ổ khóa). Format: Bearer <token>",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // SpringDoc will automatically pick up these annotations to build the Swagger UI
}
