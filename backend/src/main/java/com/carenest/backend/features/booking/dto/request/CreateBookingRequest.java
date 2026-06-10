package com.carenest.backend.features.booking.dto.request;

import com.carenest.backend.features.booking.enums.BookingRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBookingRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "healthProfileId is required")
    private Long healthProfileId;

    @NotNull(message = "Request type is required")
    private BookingRequestType requestType;

    @NotBlank(message = "Note is required")
    @Size(max = 2000, message = "Patient note must be at most 2000 characters")
    private String note;

    @Size(max = 300, message = "Preferred schedule must be at most 300 characters")
    private String preferredTimeNote;
}
