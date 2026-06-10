package com.carenest.backend.features.booking.dto.response;

import com.carenest.backend.features.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultationThreadInboxResponse {
    private Long id;
    private Long latestBookingId;
    private Long patientId;
    private String patientFullName;
    private String patientAvatarUrl;
    private Long doctorId;
    private String doctorFullName;
    private String doctorAvatarUrl;
    private BookingStatus status;
}
