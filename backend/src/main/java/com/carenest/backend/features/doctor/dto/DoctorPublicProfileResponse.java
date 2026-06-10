package com.carenest.backend.features.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPublicProfileResponse {
    private Long id;
    private String fullName;
    private String avatarUrl;
    private String specialty;
    private String hospitalName;
    private String certificationNumber;
    private boolean isVerified;
    private Instant verifiedAt;
}
