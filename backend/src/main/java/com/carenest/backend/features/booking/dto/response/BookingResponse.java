package com.carenest.backend.features.booking.dto.response;

import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long patientId;
    private String patientFullName;
    private String patientAvatarUrl;
    private Long doctorId;
    private String doctorFullName;
    private String doctorAvatarUrl;
    private BookingRequestType requestType;
    private BookingStatus status;
    private String note;
    private String preferredTimeNote;
    private String rejectReason;
    private Instant createdAt;
}
