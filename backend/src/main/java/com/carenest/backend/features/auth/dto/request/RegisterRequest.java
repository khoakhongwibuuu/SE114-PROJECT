package com.carenest.backend.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Vui lÃ²ng nháº­p email")
    @Email(message = "Email khÃ´ng Ä‘Ãºng Ä‘á»‹nh dáº¡ng")
    private String email;

    @NotBlank(message = "Vui lÃ²ng nháº­p máº­t kháº©u")
    @Size(min = 6, message = "Máº­t kháº©u pháº£i cÃ³ Ã­t nháº¥t 6 kÃ½ tá»±")
    private String password;

    @NotBlank(message = "Vui lÃ²ng nháº­p há» vÃ  tÃªn")
    private String fullName;

    @Size(max = 20, message = "Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 20 kÃ½ tá»±")
    private String phoneNumber;
}
