package com.carenest.backend.features.vaccination.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CreateVaccinationRequest {

    @NotBlank(message = "TÃªn váº¯c xin khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Schema(description = "TÃªn váº¯c xin", example = "Váº¯c-xin 6 trong 1 Hexaxim")
    private String vaccineName;

    @NotNull(message = "Sá»‘ thá»© tá»± mÅ©i tiÃªm khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Schema(description = "MÅ©i thá»© máº¥y (vÃ­ dá»¥: 1, 2, 99 cho mÅ©i nháº¯c láº¡i)", example = "1")
    private Integer doseNumber;

    @NotBlank(message = "Tráº¡ng thÃ¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Schema(description = "Tráº¡ng thÃ¡i mÅ©i tiÃªm (COMPLETED hoáº·c PENDING)", example = "COMPLETED")
    private String status;

    @NotNull(message = "NgÃ y tiÃªm/dá»± kiáº¿n khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Schema(description = "NgÃ y tiÃªm thá»±c táº¿ hoáº·c ngÃ y háº¹n dá»± kiáº¿n", example = "2026-05-18")
    private LocalDate date;

    @Schema(description = "Äá»‹a Ä‘iá»ƒm tiÃªm", example = "VNVC HoÃ ng VÄƒn Thá»¥")
    private String location;

    @Schema(description = "Ghi chÃº", example = "Sau khi tiÃªm nhá»› theo dÃµi nhiá»‡t Ä‘á»™")
    private String notes;
}
