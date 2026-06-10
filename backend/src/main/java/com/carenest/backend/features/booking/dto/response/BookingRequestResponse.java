package com.carenest.backend.features.booking.dto.response;

import com.carenest.backend.features.booking.enums.BookingRequestStatus;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class BookingRequestResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String doctorHospitalName;
    private Long healthProfileId;
    private String healthProfileName;
    private BookingRequestType type;
    private BookingRequestStatus status;
    private String preferredSchedule;
    private String patientNote;
    private Instant scheduledAt;
    private String confirmedLocation;
    private String confirmedNote;
    private String rejectionReason;
    private String cancellationReason;
    private Long appointmentId;
    private String appointmentStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
