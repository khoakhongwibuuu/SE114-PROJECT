package com.carenest.backend.features.cabinet.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetMedicineUpdateRequest {

    @Size(max = 200, message = "TÃªn thuá»‘c khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 200 kÃ½ tá»±")
    private String medicineName;

    @Min(value = 0, message = "Sá»‘ lÆ°á»£ng khÃ´ng Ä‘Æ°á»£c Ã¢m")
    private Integer quantity;

    @Size(max = 50, message = "ÄÆ¡n vá»‹ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 50 kÃ½ tá»±")
    private String unit;

    @FutureOrPresent(message = "NgÃ y háº¿t háº¡n khÃ´ng Ä‘Æ°á»£c náº±m trong quÃ¡ khá»©")
    private LocalDate expiryDate;

    private String notes;
}
