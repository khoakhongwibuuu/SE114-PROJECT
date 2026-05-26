package com.carenest.backend.features.family.dto.request;

import com.carenest.backend.features.family.enums.FamilyRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoleRequest {

    @NotNull(message = "Vai trÃ² khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private FamilyRole role;
}
