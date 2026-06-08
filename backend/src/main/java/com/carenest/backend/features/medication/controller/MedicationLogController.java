package com.carenest.backend.features.medication.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.medication.dto.request.CheckInMedicationRequest;
import com.carenest.backend.features.medication.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/medication-logs")
@RequiredArgsConstructor
@Tag(name = "Medication Log", description = "Đánh dấu lịch uống thuốc hằng ngày (check-in)")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class MedicationLogController {

    private final MedicationService medicationService;

    @PostMapping("/{logId}/check-in")
    @Operation(summary = "Check-in (đánh dấu đã uống hoặc bỏ qua) một cữ thuốc")
    public ApiResponse<Void> checkInMedicationLog(
            @PathVariable("logId") Long logId,
            @Valid @RequestBody CheckInMedicationRequest request) {
        medicationService.checkInMedicationLog(logId, request);
        return ApiResponse.success("Ghi nhận trạng thái uống thuốc thành công", null);
    }
}
