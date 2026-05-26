package com.carenest.backend.features.appointment.dto.request;

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

    @NotNull(message = "Vui lÃ²ng chá»n há»“ sÆ¡ sá»©c khá»e")
    private Long healthProfileId;
}
