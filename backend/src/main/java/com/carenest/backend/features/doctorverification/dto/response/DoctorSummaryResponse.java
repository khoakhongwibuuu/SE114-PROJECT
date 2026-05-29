package com.carenest.backend.features.doctorverification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DoctorSummaryResponse {
    private Long id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String certificationNumber;
    private String specialty;
    private String hospitalName;
    private String documentUrl;
    private Instant approvedAt;
}
