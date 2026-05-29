package com.carenest.backend.features.appointment.dto.request;

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

    @NotNull(message = "Vui lÃ²ng chá»n há»“ sÆ¡ sá»©c khá»e")
    private Long healthProfileId;

    @Size(max = 200, message = "TÃªn bÃ¡c sÄ© khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 200 kÃ½ tá»±")
    private String doctorName;

    @Size(max = 200, message = "TÃªn bá»‡nh viá»‡n khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 200 kÃ½ tá»±")
    private String hospitalName;

    private String address;

    @NotNull(message = "Vui lÃ²ng chá»n ngÃ y khÃ¡m")
    @FutureOrPresent(message = "NgÃ y khÃ¡m khÃ´ng Ä‘Æ°á»£c náº±m trong quÃ¡ khá»©")
    private Instant appointmentDate;

    private String notes;
}
