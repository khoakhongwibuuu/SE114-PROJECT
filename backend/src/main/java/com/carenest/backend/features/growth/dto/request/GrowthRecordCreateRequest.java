package com.carenest.backend.features.growth.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecordCreateRequest {

    @NotNull(message = "Vui lÃ²ng nháº­p ngÃ y ghi nháº­n")
    private LocalDate recordDate;

    @NotNull(message = "Vui lÃ²ng nháº­p cÃ¢n náº·ng")
    @DecimalMin(value = "1.0", message = "CÃ¢n náº·ng pháº£i tá»‘i thiá»ƒu 1.0 kg")
    @DecimalMax(value = "150.0", message = "CÃ¢n náº·ng khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 150.0 kg")
    private BigDecimal weightKg;

    @NotNull(message = "Vui lÃ²ng nháº­p chiá»u cao")
    @DecimalMin(value = "30.0", message = "Chiá»u cao pháº£i tá»‘i thiá»ƒu 30.0 cm")
    @DecimalMax(value = "250.0", message = "Chiá»u cao khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 250.0 cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "20.0", message = "VÃ²ng Ä‘áº§u pháº£i tá»‘i thiá»ƒu 20.0 cm")
    @DecimalMax(value = "70.0", message = "VÃ²ng Ä‘áº§u khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 70.0 cm")
    private BigDecimal headCircumferenceCm;

    private String notes;
}
