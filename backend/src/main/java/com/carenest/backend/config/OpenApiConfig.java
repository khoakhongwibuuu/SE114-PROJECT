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
                description = "TÃ i liá»‡u API cho á»©ng dá»¥ng quáº£n lÃ½ sá»©c khá»e gia Ä‘Ã¬nh CareNest.",
                contact = @Contact(
                        name = "Tuáº¥n Kiá»‡t & Anh Khoa",
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
        description = "Nháº­p JWT Token táº¡i Ä‘Ã¢y Ä‘á»ƒ truy cáº­p cÃ¡c API bá»‹ khÃ³a báº£o máº­t (cÃ³ á»• khÃ³a). Format: Bearer <token>",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // SpringDoc will automatically pick up these annotations to build the Swagger UI
}
