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

    @Size(max = 200, message = "Tên bác sĩ không được vượt quá 200 ký tự")
    private String doctorName;

    @Size(max = 200, message = "Tên bệnh viện không được vượt quá 200 ký tự")
    private String hospitalName;

    private String address;

    @FutureOrPresent(message = "Ngày khám không được nằm trong quá khứ")
    private Instant appointmentDate;

    private String notes;
}
