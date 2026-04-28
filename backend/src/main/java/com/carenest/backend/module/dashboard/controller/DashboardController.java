package com.carenest.backend.module.dashboard.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.dashboard.dto.response.DashboardResponse;
import com.carenest.backend.module.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Lấy dữ liệu Dashboard", description = "Lấy tổng quan công việc hôm nay của gia đình")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Lỗi dữ liệu đầu vào (Ví dụ: ID không hợp lệ)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Không có quyền truy cập (Token không hợp lệ hoặc hết hạn)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Người dùng không thuộc gia đình này")
    })
    @GetMapping("/today")
    public ResponseEntity<com.carenest.backend.common.dto.ApiResponse<DashboardResponse>> getDashboardOverview(@RequestParam("familyId") Long familyId) {
        DashboardResponse response = dashboardService.getDashboardOverview(familyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu Dashboard thành công", response));
    }
}
