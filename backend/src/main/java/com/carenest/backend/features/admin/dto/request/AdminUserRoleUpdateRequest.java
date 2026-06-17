package com.carenest.backend.features.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRoleUpdateRequest {
    @NotBlank(message = "Role cannot be blank")
    private String role;

    @NotBlank(message = "Reason cannot be blank")
    private String reason;
}
