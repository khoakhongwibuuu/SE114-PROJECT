package com.carenest.backend.module.family.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.module.family.dto.request.InviteMemberRequest;
import com.carenest.backend.module.family.dto.request.JoinFamilyByCodeRequest;
import com.carenest.backend.module.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.module.family.dto.response.FamilyJoinCodeResponse;
import com.carenest.backend.module.family.dto.response.FamilyResponse;
import com.carenest.backend.module.family.dto.response.FamilySummaryResponse;
import com.carenest.backend.module.family.enums.FamilyRole;
import com.carenest.backend.module.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/families")
@RequiredArgsConstructor
@Tag(name = "Family", description = "Family management")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class FamilyController {

    private final FamilyService familyService;

    @GetMapping
    @Operation(summary = "Get current user's primary family (legacy — prefer /my-list)")
    public ApiResponse<FamilyDetailResponse> getMyFamily() {
        return ApiResponse.success(familyService.getMyFamily());
    }

    @GetMapping("/my-list")
    @Operation(summary = "Get all families the current user belongs to")
    public ApiResponse<List<FamilySummaryResponse>> getMyFamilies() {
        return ApiResponse.success(familyService.getMyFamilies());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new family")
    public ApiResponse<FamilyResponse> createFamily(@Valid @RequestBody CreateFamilyRequest request) {
        return ApiResponse.success("Tạo gia đình thành công", familyService.createFamily(request));
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
        return ApiResponse.success("Đã gửi lời mời gia đình", null);
    }

    @PutMapping("/{id}/members/{memberId}/role")
    @Operation(summary = "Update a family member role")
    public ApiResponse<Void> updateMemberRole(
            @PathVariable("id") Long id,
            @PathVariable("memberId") Long memberId,
            @Valid @RequestBody UpdateRoleRequest request) {
        familyService.updateMemberRole(id, memberId, request);
        return ApiResponse.success("Cập nhật vai trò thành viên thành công", null);
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

    @PostMapping(value = "/join-by-qr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Join a family by QR image")
    public ApiResponse<FamilyDetailResponse> joinByQr(
            @RequestPart("image") MultipartFile image,
            @RequestParam(value = "role", required = false) FamilyRole role) {
        return ApiResponse.success(familyService.joinByQr(image, role));
    }
}
