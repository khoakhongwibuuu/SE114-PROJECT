package com.carenest.backend.module.appointment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentMemberRequest {

    @NotNull(message = "Vui lòng chọn hồ sơ sức khỏe")
    private Long healthProfileId;
}
