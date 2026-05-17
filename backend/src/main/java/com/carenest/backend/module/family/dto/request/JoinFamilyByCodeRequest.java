package com.carenest.backend.module.family.dto.request;

import com.carenest.backend.module.family.enums.FamilyRole;
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

    @NotBlank(message = "Join code is required")
    private String joinCode;

    private FamilyRole role;
}
