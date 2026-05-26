package com.carenest.backend.features.auth.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Vui lÃ²ng nháº­p há» vÃ  tÃªn")
    private String fullName;

    private String phone;

    private LocalDate dateOfBirth;

    private String gender;

    private String avatarUrl;
}
