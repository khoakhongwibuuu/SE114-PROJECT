package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportPostRequest {

    @NotBlank(message = "Vui lÃ²ng nháº­p lÃ½ do bÃ¡o cÃ¡o")
    @Size(max = 500, message = "LÃ½ do bÃ¡o cÃ¡o khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String reason;
}
