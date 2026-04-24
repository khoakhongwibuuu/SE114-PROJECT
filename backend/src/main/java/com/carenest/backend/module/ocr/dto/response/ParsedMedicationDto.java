package com.carenest.backend.module.ocr.dto.response;

import com.carenest.backend.module.medication.enums.MedicationFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedMedicationDto {
    private String medicineName;
    private Integer totalQuantity;
    private String unit;
    private String dosage;
    private MedicationFrequency frequency;
    private Integer timesPerDay;
    private Integer durationDays;
    private String notes;
}
