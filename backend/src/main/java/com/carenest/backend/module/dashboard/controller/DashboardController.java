package com.carenest.backend.module.dashboard.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.dashboard.dto.response.DashboardResponse;
import com.carenest.backend.module.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardOverview(@RequestParam("familyId") Long familyId) {
        DashboardResponse response = dashboardService.getDashboardOverview(familyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu Dashboard thành công", response));
    }
}
