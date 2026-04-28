package com.carenest.backend.module.appointment.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentUpdateRequest {

    @Size(max = 200, message = "Doctor name cannot exceed 200 characters")
    private String doctorName;

    @Size(max = 200, message = "Hospital name cannot exceed 200 characters")
    private String hospitalName;

    private String address;

    @FutureOrPresent(message = "Appointment date cannot be in the past")
    private Instant appointmentDate;

    private String notes;
}
