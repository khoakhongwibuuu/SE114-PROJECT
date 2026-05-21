package com.carenest.backend.module.medication.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.medication.dto.request.CheckInMedicationRequest;
import com.carenest.backend.module.medication.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medication-logs")
@RequiredArgsConstructor
@Tag(name = "Medication Log", description = "Đánh dấu lịch uống thuốc hàng ngày (Check-in)")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class MedicationLogController {

    private final MedicationService medicationService;

    @PostMapping("/{logId}/check-in")
    @Operation(summary = "Check-in (Đánh dấu đã uống hoặc bỏ qua) một cữ thuốc")
    public ApiResponse<Void> checkInMedicationLog(
            @PathVariable("logId") Long logId,
            @Valid @RequestBody CheckInMedicationRequest request) {
        medicationService.checkInMedicationLog(logId, request);
        return ApiResponse.success("Ghi nhận trạng thái uống thuốc thành công", null);
    }
}
