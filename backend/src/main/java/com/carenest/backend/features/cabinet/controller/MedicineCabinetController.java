package com.carenest.backend.features.cabinet.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.cabinet.dto.request.CabinetCreateRequest;
import com.carenest.backend.features.cabinet.dto.request.CabinetMedicineRequest;
import com.carenest.backend.features.cabinet.dto.request.CabinetMedicineUpdateRequest;
import com.carenest.backend.features.cabinet.dto.response.CabinetMedicineResponse;
import com.carenest.backend.features.cabinet.dto.response.MedicineCabinetResponse;
import com.carenest.backend.features.cabinet.service.MedicineCabinetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class MedicineCabinetController {

    private final MedicineCabinetService medicineCabinetService;

    @PostMapping("/cabinets")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MedicineCabinetResponse> createCabinet(@Valid @RequestBody CabinetCreateRequest request) {
        MedicineCabinetResponse response = medicineCabinetService.createCabinet(request);
        return ApiResponse.success("Táº¡o tá»§ thuá»‘c thÃ nh cÃ´ng", response);
    }

    @GetMapping("/families/{familyId}/cabinets")
    public ApiResponse<MedicineCabinetResponse> getFamilyCabinet(@PathVariable("familyId") Long familyId) {
        MedicineCabinetResponse response = medicineCabinetService.getFamilyCabinet(familyId);
        return ApiResponse.success("Láº¥y tá»§ thuá»‘c gia Ä‘Ã¬nh thÃ nh cÃ´ng", response);
    }

    @PostMapping("/cabinets/{id}/medicines")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CabinetMedicineResponse> addMedicine(
            @PathVariable("id") Long id,
            @Valid @RequestBody CabinetMedicineRequest request) {
        CabinetMedicineResponse response = medicineCabinetService.addMedicine(id, request);
        return ApiResponse.success("ÄÃ£ thÃªm thuá»‘c vÃ o tá»§ thuá»‘c", response);
    }

    @PutMapping("/cabinets/{id}/medicines/{medicineId}")
    public ApiResponse<CabinetMedicineResponse> updateMedicine(
            @PathVariable("id") Long id,
            @PathVariable("medicineId") Long medicineId,
            @Valid @RequestBody CabinetMedicineUpdateRequest request) {
        CabinetMedicineResponse response = medicineCabinetService.updateMedicine(id, medicineId, request);
        return ApiResponse.success("Cáº­p nháº­t thuá»‘c thÃ nh cÃ´ng", response);
    }

    @DeleteMapping("/cabinets/{id}/medicines/{medicineId}")
    public ApiResponse<Void> removeMedicine(
            @PathVariable("id") Long id,
            @PathVariable("medicineId") Long medicineId) {
        medicineCabinetService.removeMedicine(id, medicineId);
        return ApiResponse.success("ÄÃ£ xÃ³a thuá»‘c khá»i tá»§ thuá»‘c", null);
    }

    @GetMapping("/cabinets/{id}/medicines")
    public ApiResponse<List<CabinetMedicineResponse>> getMedicines(
            @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "all") String status) {
        List<CabinetMedicineResponse> responses = medicineCabinetService.getMedicines(id, status);
        return ApiResponse.success("Láº¥y danh sÃ¡ch thuá»‘c thÃ nh cÃ´ng", responses);
    }
}
