package com.carenest.backend.module.family.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.module.family.dto.request.InviteMemberRequest;
import com.carenest.backend.module.family.dto.request.JoinFamilyByCodeRequest;
import com.carenest.backend.module.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.module.family.dto.response.FamilyJoinCodeResponse;
import com.carenest.backend.module.family.dto.response.FamilyResponse;
import com.carenest.backend.module.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/families")
@RequiredArgsConstructor
@Tag(name = "Family", description = "Family management")
@SecurityRequirement(name = "bearerAuth")
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping
    @Operation(summary = "Get current user's family")
    public ApiResponse<FamilyDetailResponse> getMyFamily() {
        return ApiResponse.success(familyService.getMyFamily());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new family")
    public ApiResponse<FamilyResponse> createFamily(@Valid @RequestBody CreateFamilyRequest request) {
        return ApiResponse.success("Family created successfully", familyService.createFamily(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get family details")
    public ApiResponse<FamilyDetailResponse> getFamilyById(@PathVariable("id") Long id) {
        return ApiResponse.success(familyService.getFamilyById(id));
    }

    @PostMapping("/{id}/invitations")
    @Operation(summary = "Invite a member by email")
    public ApiResponse<Void> inviteMember(
            @PathVariable("id") Long id,
            @Valid @RequestBody InviteMemberRequest request) {
        familyService.inviteMember(id, request);
        return ApiResponse.success("Invitation sent successfully", null);
    }

    @PutMapping("/{id}/members/{memberId}/role")
    @Operation(summary = "Update a family member role")
    public ApiResponse<Void> updateMemberRole(
            @PathVariable("id") Long id,
            @PathVariable("memberId") Long memberId,
            @Valid @RequestBody UpdateRoleRequest request) {
        familyService.updateMemberRole(id, memberId, request);
        return ApiResponse.success("Member role updated successfully", null);
    }

    @GetMapping("/join-code")
    @Operation(summary = "Get current family's join code")
    public ApiResponse<FamilyJoinCodeResponse> getJoinCode() {
        return ApiResponse.success(familyService.getJoinCode());
    }

    @PostMapping("/join-code/rotate")
    @Operation(summary = "Rotate current family's join code")
    public ApiResponse<FamilyJoinCodeResponse> rotateJoinCode() {
        return ApiResponse.success(familyService.rotateJoinCode());
    }

    @PostMapping("/join-by-code")
    @Operation(summary = "Join a family by code")
    public ApiResponse<FamilyDetailResponse> joinByCode(@Valid @RequestBody JoinFamilyByCodeRequest request) {
        return ApiResponse.success(familyService.joinByCode(request));
    }
}
