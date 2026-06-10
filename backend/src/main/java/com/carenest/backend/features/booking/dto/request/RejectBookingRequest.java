package com.carenest.backend.features.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectBookingRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}
