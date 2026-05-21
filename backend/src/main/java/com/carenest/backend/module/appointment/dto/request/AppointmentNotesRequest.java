package com.carenest.backend.module.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentNotesRequest {

    @NotBlank(message = "Ghi chú kết quả không được để trống")
    private String resultNotes;
}
