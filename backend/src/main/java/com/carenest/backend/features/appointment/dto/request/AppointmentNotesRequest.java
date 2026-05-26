package com.carenest.backend.features.appointment.dto.request;

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

    @NotBlank(message = "Ghi chÃº káº¿t quáº£ khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String resultNotes;
}
