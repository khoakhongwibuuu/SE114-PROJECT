package com.carenest.backend.features.admin.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.admin.dto.request.AdminUserStatusUpdateRequest;
import com.carenest.backend.features.admin.dto.response.AdminDashboardStatsResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserStatusUpdateResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserSummaryResponse;
import com.carenest.backend.features.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin Management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard-stats")
    @Operation(summary = "Get admin dashboard statistics")
    public ApiResponse<AdminDashboardStatsResponse> getDashboardStats() {
        AdminDashboardStatsResponse response = adminService.getDashboardStats();
        return ApiResponse.success(response);
    }

    @GetMapping("/users")
    @Operation(summary = "Get paginated list of users")
    public ApiResponse<PageResponse<AdminUserSummaryResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        PageResponse<AdminUserSummaryResponse> response = adminService.getUsers(page, size, search);
        return ApiResponse.success(response);
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Update user status (ACTIVE/BANNED)")
    public ApiResponse<AdminUserStatusUpdateResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody @Valid AdminUserStatusUpdateRequest request) {
        AdminUserStatusUpdateResponse response = adminService.updateUserStatus(userId, request);
        return ApiResponse.success("User status updated successfully", response);
    }
}
