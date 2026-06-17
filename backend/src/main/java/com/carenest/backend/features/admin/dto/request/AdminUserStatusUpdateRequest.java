package com.carenest.backend.features.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserStatusUpdateRequest {
    @NotBlank(message = "Status cannot be blank")
    private String status;

    @NotBlank(message = "Reason cannot be blank")
    private String reason;
}
