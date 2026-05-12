package com.carenest.backend.module.auth.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    private String phone;
    
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String avatarUrl;
}
