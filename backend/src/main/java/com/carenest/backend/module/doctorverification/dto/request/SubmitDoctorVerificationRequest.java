package com.carenest.backend.module.doctorverification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitDoctorVerificationRequest {

    @NotBlank(message = "Certification number is required")
    @Size(max = 100, message = "Certification number must be at most 100 characters")
    private String certificationNumber;

    @NotBlank(message = "Specialty is required")
    @Size(max = 100, message = "Specialty must be at most 100 characters")
    private String specialty;

    @NotBlank(message = "Hospital name is required")
    @Size(max = 200, message = "Hospital name must be at most 200 characters")
    private String hospitalName;

    @NotBlank(message = "Document URL is required")
    @Size(max = 1000, message = "Document URL must be at most 1000 characters")
    private String documentUrl;
}
