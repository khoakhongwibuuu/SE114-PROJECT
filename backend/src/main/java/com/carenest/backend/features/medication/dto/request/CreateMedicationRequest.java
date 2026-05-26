package com.carenest.backend.features.medication.dto.request;

import com.carenest.backend.features.medication.enums.MedicationFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateMedicationRequest {

    @NotBlank(message = "TÃªn thuá»‘c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private String medicineName;

    private String dosage;

    @NotNull(message = "Táº§n suáº¥t uá»‘ng thuá»‘c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private MedicationFrequency frequency;

    private Integer timesPerDay;

    // Danh sÃ¡ch giá» uá»‘ng thuá»‘c, VD: ["08:00", "13:00", "20:00"]
    private List<String> timeSlots;

    @NotNull(message = "NgÃ y báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate startDate;

    private LocalDate endDate;

    private String notes;
}
