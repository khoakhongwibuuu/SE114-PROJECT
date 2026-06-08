package com.carenest.backend.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Vui lòng nhập họ và tên")
    private String fullName;

    private String phone;

    private LocalDate dateOfBirth;

    private String gender;

    private String avatarUrl;
}
