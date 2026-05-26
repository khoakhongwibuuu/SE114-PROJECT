package com.carenest.backend.features.medication.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.medication.dto.request.CheckInMedicationRequest;
import com.carenest.backend.features.medication.service.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medication-logs")
@RequiredArgsConstructor
@Tag(name = "Medication Log", description = "ÄÃ¡nh dáº¥u lá»‹ch uá»‘ng thuá»‘c hÃ ng ngÃ y (Check-in)")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class MedicationLogController {

    private final MedicationService medicationService;

    @PostMapping("/{logId}/check-in")
    @Operation(summary = "Check-in (ÄÃ¡nh dáº¥u Ä‘Ã£ uá»‘ng hoáº·c bá» qua) má»™t cá»¯ thuá»‘c")
    public ApiResponse<Void> checkInMedicationLog(
            @PathVariable("logId") Long logId,
            @Valid @RequestBody CheckInMedicationRequest request) {
        medicationService.checkInMedicationLog(logId, request);
        return ApiResponse.success("Ghi nháº­n tráº¡ng thÃ¡i uá»‘ng thuá»‘c thÃ nh cÃ´ng", null);
    }
}
