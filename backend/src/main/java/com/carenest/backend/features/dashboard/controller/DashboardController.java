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

    @Operation(summary = "Láº¥y dá»¯ liá»‡u Dashboard", description = "Láº¥y tá»•ng quan cÃ´ng viá»‡c hÃ´m nay cá»§a gia Ä‘Ã¬nh")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "ThÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Lá»—i dá»¯ liá»‡u Ä‘áº§u vÃ o (VÃ­ dá»¥: ID khÃ´ng há»£p lá»‡)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "KhÃ´ng cÃ³ quyá»n truy cáº­p (Token khÃ´ng há»£p lá»‡ hoáº·c háº¿t háº¡n)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "NgÆ°á»i dÃ¹ng khÃ´ng thuá»™c gia Ä‘Ã¬nh nÃ y")
    })
    @GetMapping({"", "/today"})
    public ResponseEntity<com.carenest.backend.core.api.ApiResponse<DashboardResponse>> getDashboardOverview(
            @RequestParam(value = "familyId", required = false) Long familyId,
            @RequestParam(value = "profileId", required = false) Long profileId) {
        Long resolvedFamilyId = familyId != null ? familyId : FamilyRequestContext.getFamilyId();
        DashboardResponse response = dashboardService.getDashboardOverview(resolvedFamilyId, profileId);
        return ResponseEntity.ok(ApiResponse.success("Láº¥y dá»¯ liá»‡u Dashboard thÃ nh cÃ´ng", response));
    }
}
