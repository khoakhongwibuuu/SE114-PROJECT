package com.carenest.backend.features.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTask {
    @Schema(description = "Loáº¡i cÃ´ng viá»‡c", example = "MEDICATION")
    private String type; // "MEDICATION" or "VACCINATION"

    @Schema(description = "TiÃªu Ä‘á» cÃ´ng viá»‡c", example = "Paracetamol 500mg")
    private String title;

    @Schema(description = "Thá»i gian thá»±c hiá»‡n", example = "2023-10-27T08:00:00Z")
    private String time;

    @Schema(description = "TÃªn thành viên gia Ä‘Ã¬nh", example = "BÃ© Na")
    private String memberName;

    @Schema(description = "ID tham chiáº¿u cá»§a cÃ´ng viá»‡c (MedicationLog ID hoáº·c VaccinationDose ID)", example = "123")
    private Long referenceId;

    @Schema(description = "ID há»“ sÆ¡ sá»©c khá»e cá»§a thành viên", example = "3")
    private Long profileId;

    @Schema(description = "MÃ´ táº£ phá»¥ hoáº·c tháº» nháº¯c nhá»Ÿ", example = "â³ NgÃ y mai")
    private String subtitle;
}
