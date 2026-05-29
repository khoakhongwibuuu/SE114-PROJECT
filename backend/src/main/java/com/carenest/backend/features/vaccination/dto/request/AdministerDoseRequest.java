package com.carenest.backend.features.vaccination.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdministerDoseRequest {

    @NotNull(message = "NgÃ y tiÃªm khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Schema(description = "NgÃ y thá»±c táº¿ Ä‘Ã£ tiÃªm", example = "2023-11-01")
    private LocalDate dateAdministered;

    @Schema(description = "Äá»‹a Ä‘iá»ƒm tiÃªm", example = "VNVC HoÃ ng VÄƒn Thá»¥")
    private String location;

    @Schema(description = "NgÆ°á»i thá»±c hiá»‡n tiÃªm", example = "BS. Nguyá»…n VÄƒn A")
    private String administeredBy;

    @Schema(description = "Ghi chÃº sau tiÃªm", example = "BÃ© khÃ³c nhiá»u, khÃ´ng sá»‘t")
    private String notes;
}
