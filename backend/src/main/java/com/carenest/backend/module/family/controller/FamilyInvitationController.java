package com.carenest.backend.module.family.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.module.family.dto.response.FamilyInvitationResponse;
import com.carenest.backend.module.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
@Tag(name = "Family Invitation", description = "Family invitation management")
@SecurityRequirement(name = "bearerAuth")
public class FamilyInvitationController {

    private final FamilyService familyService;

    @GetMapping("/received")
    @Operation(summary = "Get received family invitations")
    public ApiResponse<List<FamilyInvitationResponse>> getReceivedInvitations() {
        return ApiResponse.success(familyService.getReceivedInvitations());
    }

    @GetMapping("/sent")
    @Operation(summary = "Get sent family invitations")
    public ApiResponse<List<FamilyInvitationResponse>> getSentInvitations() {
        return ApiResponse.success(familyService.getSentInvitations());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Accept or reject a family invitation")
    public ApiResponse<Void> handleInvitation(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateInvitationRequest request) {
        familyService.handleInvitation(id, request);
        return ApiResponse.success("Invitation handled successfully", null);
    }
}
