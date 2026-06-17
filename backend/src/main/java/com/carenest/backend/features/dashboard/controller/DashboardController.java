package com.carenest.backend.features.dashboard.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.dashboard.dto.response.DashboardResponse;
import com.carenest.backend.features.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.carenest.backend.features.family.context.FamilyRequestContext;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Lấy dữ liệu Dashboard", description = "Lấy tổng quan công việc hôm nay của gia đình")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Lỗi dữ liệu đầu vào (Ví dụ: ID không hợp lệ)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Không có quyền truy cập (Token không hợp lệ hoặc hết hạn)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Người dùng không thuộc gia đình này")
    })
    @GetMapping({"", "/today"})
    public ResponseEntity<com.carenest.backend.core.api.ApiResponse<DashboardResponse>> getDashboardOverview(
            @RequestParam(value = "familyId", required = false) Long familyId,
            @RequestParam(value = "profileId", required = false) Long profileId) {
        Long resolvedFamilyId = familyId != null ? familyId : FamilyRequestContext.getFamilyId();
        DashboardResponse response = dashboardService.getDashboardOverview(resolvedFamilyId, profileId);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu Dashboard thành công", response));
    }
}
