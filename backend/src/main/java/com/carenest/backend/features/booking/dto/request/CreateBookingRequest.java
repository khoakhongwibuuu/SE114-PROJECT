package com.carenest.backend.features.booking.dto.request;

import com.carenest.backend.features.booking.enums.BookingRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Request type is required")
    private BookingRequestType requestType;

    @NotBlank(message = "Note is required")
    private String note;

    private String preferredTimeNote;
}
