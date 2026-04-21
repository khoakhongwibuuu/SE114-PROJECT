package com.carenest.backend.module.family.dto.request;

import com.carenest.backend.module.family.enums.FamilyRole;
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

    @NotNull(message = "Vai trò không được để trống")
    private FamilyRole role;
}
