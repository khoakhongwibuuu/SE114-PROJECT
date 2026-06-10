package com.carenest.backend.features.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ConfirmBookingScheduleRequest {

    @NotNull(message = "scheduledAt is required")
    private Instant scheduledAt;

    @Size(max = 300, message = "Confirmed location must be at most 300 characters")
    private String confirmedLocation;

    @Size(max = 2000, message = "Confirmed note must be at most 2000 characters")
    private String confirmedNote;
}
