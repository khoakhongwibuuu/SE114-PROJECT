package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupMemberRoleRequest {
    @NotBlank(message = "Group role cannot be blank")
    private String role;
}
