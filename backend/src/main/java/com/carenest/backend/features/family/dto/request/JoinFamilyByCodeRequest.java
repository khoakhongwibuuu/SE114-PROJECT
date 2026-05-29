package com.carenest.backend.features.family.dto.request;

import com.carenest.backend.features.family.enums.FamilyRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinFamilyByCodeRequest {

    @NotBlank(message = "Vui lÃ²ng nháº­p mÃ£ gia Ä‘Ã¬nh")
    private String joinCode;

    private FamilyRole role;
}
