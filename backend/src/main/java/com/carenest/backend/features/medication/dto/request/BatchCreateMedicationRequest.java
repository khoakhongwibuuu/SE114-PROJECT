package com.carenest.backend.features.medication.dto.request;

import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchCreateMedicationRequest {
    @NotNull(message = "Health Profile ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long healthProfileId;

    @NotNull(message = "Family ID khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private Long familyId;

    @NotEmpty(message = "Danh sÃ¡ch thuá»‘c khÃ´ng Ä‘Æ°á»£c rá»—ng")
    private List<ParsedMedicationDto> medications;
}
