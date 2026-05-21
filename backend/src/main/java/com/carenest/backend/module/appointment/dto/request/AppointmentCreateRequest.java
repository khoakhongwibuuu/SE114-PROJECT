package com.carenest.backend.module.appointment.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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
public class AppointmentCreateRequest {

    @NotNull(message = "Vui lòng chọn hồ sơ sức khỏe")
    private Long healthProfileId;

    @Size(max = 200, message = "Tên bác sĩ không được vượt quá 200 ký tự")
    private String doctorName;

    @Size(max = 200, message = "Tên bệnh viện không được vượt quá 200 ký tự")
    private String hospitalName;

    private String address;

    @NotNull(message = "Vui lòng chọn ngày khám")
    @FutureOrPresent(message = "Ngày khám không được nằm trong quá khứ")
    private Instant appointmentDate;

    private String notes;
}
