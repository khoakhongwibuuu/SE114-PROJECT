package com.carenest.backend.features.vaccination.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.features.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.features.vaccination.dto.response.VaccinationRecordResponse;
import com.carenest.backend.features.vaccination.service.VaccinationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Vaccination", description = "Quáº£n lÃ½ lá»‹ch tiÃªm chá»§ng")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    @PostMapping("/health-profiles/{profileId}/vaccinations")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.Operation(summary = "Táº¡o lá»‹ch tiÃªm chá»§ng má»›i (Tá»± Ä‘á»™ng sinh sáºµn cÃ¡c mÅ©i tiÃªm)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Táº¡o lá»‹ch tiÃªm chá»§ng thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dá»¯ liá»‡u Ä‘áº§u vÃ o khÃ´ng há»£p lá»‡"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KhÃ´ng tÃ¬m tháº¥y há»“ sÆ¡ sá»©c khá»e")
    })
    public com.carenest.backend.core.api.ApiResponse<VaccinationRecordResponse> createVaccinationPlan(
            @PathVariable("profileId") Long profileId,
            @Valid @RequestBody CreateVaccinationRequest request) {
        VaccinationRecordResponse response = vaccinationService.createVaccinationPlan(profileId, request);
        return ApiResponse.success("Táº¡o lá»‹ch tiÃªm chá»§ng thÃ nh cÃ´ng", response);
    }

    @GetMapping("/health-profiles/{profileId}/vaccinations")
    @io.swagger.v3.oas.annotations.Operation(summary = "Láº¥y lá»‹ch sá»­ tiÃªm chá»§ng cá»§a má»™t há»“ sÆ¡")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Láº¥y lá»‹ch sá»­ thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KhÃ´ng tÃ¬m tháº¥y há»“ sÆ¡ sá»©c khá»e")
    })
    public com.carenest.backend.core.api.ApiResponse<List<VaccinationRecordResponse>> getVaccinationHistory(@PathVariable("profileId") Long profileId) {
        List<VaccinationRecordResponse> response = vaccinationService.getVaccinationHistory(profileId);
        return ApiResponse.success(response);
    }

    @PutMapping("/vaccination-doses/{doseId}/administer")
    @io.swagger.v3.oas.annotations.Operation(summary = "Ghi nháº­n Ä‘Ã£ tiÃªm má»™t mÅ©i (Tá»± Ä‘á»™ng tá»‹nh tiáº¿n ngÃ y cho cÃ¡c mÅ©i sau)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cáº­p nháº­t thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "MÅ©i tiÃªm Ä‘Ã£ Ä‘Æ°á»£c hoÃ n thÃ nh trÆ°á»›c Ä‘Ã³"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "KhÃ´ng tÃ¬m tháº¥y mÅ©i tiÃªm")
    })
    public com.carenest.backend.core.api.ApiResponse<VaccinationRecordResponse> administerDose(
            @PathVariable("doseId") Long doseId,
            @Valid @RequestBody AdministerDoseRequest request) {
        VaccinationRecordResponse response = vaccinationService.administerDose(doseId, request);
        return ApiResponse.success("Cáº­p nháº­t mÅ©i tiÃªm thÃ nh cÃ´ng", response);
    }
}
