package com.carenest.backend.features.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectBookingRequest {

    @NotBlank(message = "rejectionReason is required")
    @Size(max = 2000, message = "Rejection reason must be at most 2000 characters")
    private String rejectionReason;
}
