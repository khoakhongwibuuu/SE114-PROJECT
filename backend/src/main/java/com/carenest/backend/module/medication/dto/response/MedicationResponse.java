package com.carenest.backend.module.medication.dto.response;

import com.carenest.backend.module.medication.enums.MedicationFrequency;
import com.carenest.backend.module.medication.enums.MedicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicationResponse {
    private Long id;
    private Long healthProfileId;
    private String medicineName;
    private String dosage;
    private MedicationFrequency frequency;
    private Integer timesPerDay;
    private List<String> timeSlots;
    private LocalDate startDate;
    private LocalDate endDate;
    private MedicationStatus status;
    private String notes;
}
