package com.carenest.backend.features.doctorverification.dto.response;

import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DoctorVerificationResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private String certificationNumber;
    private String specialty;
    private String hospitalName;
    private String documentUrl;
    private VerificationStatus status;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
