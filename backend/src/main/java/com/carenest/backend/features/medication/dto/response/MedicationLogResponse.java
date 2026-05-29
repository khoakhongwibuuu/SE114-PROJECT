package com.carenest.backend.features.medication.dto.response;

import com.carenest.backend.features.medication.enums.MedicationLogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicationLogResponse {
    private Long id;
    private Long medicationId;
    private String medicineName;
    private String dosage;
    private Instant scheduledTime;
    private Instant takenTime;
    private MedicationLogStatus status;
    private String notes;
}
