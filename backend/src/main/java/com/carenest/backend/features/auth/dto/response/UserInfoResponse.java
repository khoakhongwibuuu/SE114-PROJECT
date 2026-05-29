package com.carenest.backend.features.auth.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String role;
    private Boolean isVerified;
}
