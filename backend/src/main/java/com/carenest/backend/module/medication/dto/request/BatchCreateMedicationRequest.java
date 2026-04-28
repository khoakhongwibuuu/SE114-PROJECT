package com.carenest.backend.module.medication.dto.request;

import com.carenest.backend.module.ocr.dto.response.ParsedMedicationDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchCreateMedicationRequest {
    @NotNull(message = "Health Profile ID không được để trống")
    private Long healthProfileId;
    
    @NotNull(message = "Family ID không được để trống")
    private Long familyId;
    
    @NotEmpty(message = "Danh sách thuốc không được rỗng")
    private List<ParsedMedicationDto> medications;
}
