package com.carenest.backend.module.family.dto.request;

import com.carenest.backend.module.family.enums.InvitationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInvitationRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private InvitationStatus status;
}
