package com.carenest.backend.features.medication.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.medication.dto.request.BatchCreateMedicationRequest;
import com.carenest.backend.features.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.features.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.features.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.features.medication.dto.response.MedicationResponse;
import com.carenest.backend.features.medication.service.MedicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Medication", description = "Quáº£n lÃ½ Ä‘Æ¡n thuá»‘c vÃ  lá»‹ch uá»‘ng thuá»‘c")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class MedicationController {

    private final MedicationService medicationService;

    @PostMapping("/health-profiles/{profileId}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.Operation(summary = "Táº¡o Ä‘Æ¡n thuá»‘c má»›i cho má»™t há»“ sÆ¡ sá»©c khá»e")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Táº¡o Ä‘Æ¡n thuá»‘c thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dá»¯ liá»‡u khÃ´ng há»£p lá»‡ (thá»i gian, etc)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KhÃ´ng tÃ¬m tháº¥y há»“ sÆ¡ sá»©c khá»e")
    })
    public com.carenest.backend.core.api.ApiResponse<MedicationResponse> createMedication(
            @PathVariable("profileId") Long profileId,
            @Valid @RequestBody CreateMedicationRequest request) {
        MedicationResponse response = medicationService.createMedication(profileId, request);
        return ApiResponse.success("Táº¡o Ä‘Æ¡n thuá»‘c thÃ nh cÃ´ng", response);
    }

    @GetMapping("/health-profiles/{profileId}/medications")
    @io.swagger.v3.oas.annotations.Operation(summary = "Láº¥y danh sÃ¡ch táº¥t cáº£ cÃ¡c Ä‘Æ¡n thuá»‘c cá»§a má»™t há»“ sÆ¡ sá»©c khá»e")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Láº¥y danh sÃ¡ch thÃ nh cÃ´ng")
    })
    public com.carenest.backend.core.api.ApiResponse<PageResponse<MedicationResponse>> getMedicationsByProfile(
            @PathVariable("profileId") Long profileId,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<MedicationResponse> response = medicationService.getMedicationsByProfile(profileId, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/medications/today")
    @io.swagger.v3.oas.annotations.Operation(summary = "Láº¥y danh sÃ¡ch thuá»‘c cáº§n uá»‘ng HÃ”M NAY cá»§a má»™t há»“ sÆ¡")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Láº¥y danh sÃ¡ch thÃ nh cÃ´ng")
    })
    public com.carenest.backend.core.api.ApiResponse<List<MedicationLogResponse>> getMedicationsForToday(@RequestParam("profileId") Long profileId) {
        List<MedicationLogResponse> response = medicationService.getMedicationsForToday(profileId);
        return ApiResponse.success(response);
    }

    @PutMapping("/medications/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Cáº­p nháº­t thÃ´ng tá»± Ä‘á»™ng tÃ­nh láº¡i lá»‹ch uá»‘ng tÆ°Æ¡ng lai)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cáº­p nháº­t thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n thuá»‘c")
    })
    public com.carenest.backend.core.api.ApiResponse<MedicationResponse> updateMedication(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMedicationRequest request) {
        MedicationResponse response = medicationService.updateMedication(id, request);
        return ApiResponse.success("Cáº­p nháº­t Ä‘Æ¡n thuá»‘c thÃ nh cÃ´ng", response);
    }

    @PutMapping("/medications/{id}/complete")
    @io.swagger.v3.oas.annotations.Operation(summary = "ÄÃ¡nh dáº¥u káº¿t thÃºc sá»›m má»™t Ä‘Æ¡n thuá»‘c")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Káº¿t thÃºc Ä‘Æ¡n thuá»‘c thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n thuá»‘c")
    })
    public com.carenest.backend.core.api.ApiResponse<Void> completeMedication(@PathVariable("id") Long id) {
        medicationService.completeMedication(id);
        return ApiResponse.success("ÄÃ£ káº¿t thÃºc Ä‘Æ¡n thuá»‘c", null);
    }

    @DeleteMapping("/medications/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "XÃ³a hoÃ n toÃ n má»™t Ä‘Æ¡n thuá»‘c vÃ  toÃ n bá»™ lá»‹ch nháº¯c liÃªn quan")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "XÃ³a Ä‘Æ¡n thuá»‘c thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n thuá»‘c")
    })
    public com.carenest.backend.core.api.ApiResponse<Void> deleteMedication(@PathVariable("id") Long id) {
        medicationService.deleteMedication(id);
        return ApiResponse.success("ÄÃ£ xÃ³a hoÃ n toÃ n Ä‘Æ¡n thuá»‘c", null);
    }
}
