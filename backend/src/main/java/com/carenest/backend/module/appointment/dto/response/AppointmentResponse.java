package com.carenest.backend.module.appointment.dto.response;

import com.carenest.backend.module.appointment.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private Long healthProfileId;
    private String doctorName;
    private String hospitalName;
    private String address;
    private Instant appointmentDate;
    private AppointmentStatus status;
    private String notes;
    private String resultNotes;
    private Instant createdAt;
    private Instant updatedAt;
}
