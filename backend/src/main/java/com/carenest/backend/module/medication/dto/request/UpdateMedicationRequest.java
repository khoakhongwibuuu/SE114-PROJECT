package com.carenest.backend.module.medication.dto.request;

import com.carenest.backend.module.medication.enums.MedicationFrequency;
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
public class UpdateMedicationRequest {
    private String medicineName;
    private String dosage;
    private MedicationFrequency frequency;
    private Integer timesPerDay;
    private List<String> timeSlots;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
}
