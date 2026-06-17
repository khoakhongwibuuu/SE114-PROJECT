package com.carenest.backend.features.ocr.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredOcrVaccinationEntryDto {
    private String vaccineName;
    private Integer doseNumber;
    private String dateAdministered;
    private String facility;
}
