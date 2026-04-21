package com.carenest.backend.module.family.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.module.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Tag(name = "Family Invitation", description = "Xử lý lời mời tham gia tổ ấm")
@SecurityRequirement(name = "bearerAuth")
public class FamilyInvitationController {

    private final FamilyService familyService;

    @PutMapping("/{id}")
    @Operation(summary = "Chấp nhận hoặc từ chối lời mời tham gia tổ ấm")
    public ApiResponse<Void> handleInvitation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvitationRequest request) {
        familyService.handleInvitation(id, request);
        return ApiResponse.success("Xử lý lời mời thành công", null);
    }
}
