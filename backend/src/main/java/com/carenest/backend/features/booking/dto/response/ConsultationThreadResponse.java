package com.carenest.backend.features.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationThreadResponse {
    private Long id;
    private Long bookingRequestId;
    private Long patientId;
    private String patientFullName;
    private String patientAvatarUrl;
    private Long doctorId;
    private String doctorFullName;
    private String doctorAvatarUrl;
    private com.carenest.backend.features.booking.enums.BookingStatus status;
}
