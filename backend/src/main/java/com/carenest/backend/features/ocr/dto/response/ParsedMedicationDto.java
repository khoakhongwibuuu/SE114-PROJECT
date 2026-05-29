package com.carenest.backend.features.ocr.dto.response;

import com.carenest.backend.features.medication.enums.MedicationFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedMedicationDto {
    @Schema(description = "TÃªn thuá»‘c", example = "Paracetamol 500mg")
    private String medicineName;

    @Schema(description = "Tá»•ng sá»‘ lÆ°á»£ng cáº¥p phÃ¡t", example = "30")
    private Integer totalQuantity;

    @Schema(description = "ÄÆ¡n vá»‹ thuá»‘c", example = "ViÃªn")
    private String unit;

    @Schema(description = "Liá»u lÆ°á»£ng uá»‘ng má»—i láº§n", example = "1 viÃªn/láº§n")
    private String dosage;

    @Schema(description = "Táº§n suáº¥t uá»‘ng", example = "DAILY")
    private MedicationFrequency frequency;

    @Schema(description = "Sá»‘ láº§n uá»‘ng trong ngÃ y", example = "2")
    private Integer timesPerDay;

    @Schema(description = "Sá»‘ ngÃ y uá»‘ng", example = "15")
    private Integer durationDays;

    @Schema(description = "Ghi chÃº tá»« bÃ¡c sÄ©", example = "Uá»‘ng sau khi Äƒn no")
    private String notes;
}
