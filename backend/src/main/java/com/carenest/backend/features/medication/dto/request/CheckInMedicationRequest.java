package com.carenest.backend.features.medication.dto.request;

import com.carenest.backend.features.medication.enums.MedicationLogStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckInMedicationRequest {

    @NotNull(message = "Tráº¡ng thÃ¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private MedicationLogStatus status;

    private String notes;
}
