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

    @NotNull(message = "Trạng thái không được để trống")
    private MedicationLogStatus status;

    private String notes;
}
