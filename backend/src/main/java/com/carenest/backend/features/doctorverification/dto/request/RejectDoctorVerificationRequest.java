package com.carenest.backend.features.doctorverification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectDoctorVerificationRequest {

    @NotBlank(message = "Vui lòng nhập lý do từ chối")
    @Size(max = 1000, message = "Lý do từ chối không được vượt quá 1000 ký tự")
    private String rejectionReason;
}
