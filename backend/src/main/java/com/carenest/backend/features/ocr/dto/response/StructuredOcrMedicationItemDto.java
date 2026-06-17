package com.carenest.backend.features.ocr.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredOcrMedicationItemDto {
    private String name;
    private String strength;
    private String form;
    private String doseInstruction;
    private String frequency;
    private Integer durationDays;
    private String route;
}
