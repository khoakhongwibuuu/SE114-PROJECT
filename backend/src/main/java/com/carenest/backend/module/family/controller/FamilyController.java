package com.carenest.backend.module.family.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.module.family.dto.request.InviteMemberRequest;
import com.carenest.backend.module.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
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
@Tag(name = "Family", description = "Quản lý tổ ấm (gia đình)")
@SecurityRequirement(name = "bearerAuth")
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo tổ ấm mới")
    public ApiResponse<FamilyResponse> createFamily(@Valid @RequestBody CreateFamilyRequest request) {
        FamilyResponse response = familyService.createFamily(request);
        return ApiResponse.success("Tạo tổ ấm thành công", response);
    }

    @GetMapping
    @Operation(summary = "Lấy thông tin chi tiết tổ ấm của user hiện tại")
    public ApiResponse<FamilyDetailResponse> getMyFamily() {
        FamilyDetailResponse response = familyService.getMyFamily();
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết của tổ ấm (bao gồm thành viên)")
    public ApiResponse<FamilyDetailResponse> getFamilyById(@PathVariable Long id) {
        FamilyDetailResponse response = familyService.getFamilyById(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/invitations")
    @Operation(summary = "Mời thành viên mới vào tổ ấm (Chỉ Admin/Owner)")
    public ApiResponse<Void> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody InviteMemberRequest request) {
        familyService.inviteMember(id, request);
        return ApiResponse.success("Đã gửi lời mời thành công", null);
    }

    @PutMapping("/{id}/members/{memberId}/role")
    @Operation(summary = "Thay đổi vai trò của thành viên trong tổ ấm")
    public ApiResponse<Void> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateRoleRequest request) {
        familyService.updateMemberRole(id, memberId, request);
        return ApiResponse.success("Cập nhật vai trò thành công", null);
    }
}
