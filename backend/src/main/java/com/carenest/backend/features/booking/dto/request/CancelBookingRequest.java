package com.carenest.backend.features.booking.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelBookingRequest {

    @Size(max = 2000, message = "Cancellation reason must be at most 2000 characters")
    private String cancellationReason;
}
