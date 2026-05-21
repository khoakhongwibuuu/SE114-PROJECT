package com.carenest.backend.module.cabinet.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.cabinet.dto.request.CabinetCreateRequest;
import com.carenest.backend.module.cabinet.dto.request.CabinetMedicineRequest;
import com.carenest.backend.module.cabinet.dto.request.CabinetMedicineUpdateRequest;
import com.carenest.backend.module.cabinet.dto.response.CabinetMedicineResponse;
import com.carenest.backend.module.cabinet.dto.response.MedicineCabinetResponse;
import com.carenest.backend.module.cabinet.service.MedicineCabinetService;
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
        return ApiResponse.success("Tạo tủ thuốc thành công", response);
    }

    @GetMapping("/families/{familyId}/cabinets")
    public ApiResponse<MedicineCabinetResponse> getFamilyCabinet(@PathVariable("familyId") Long familyId) {
        MedicineCabinetResponse response = medicineCabinetService.getFamilyCabinet(familyId);
        return ApiResponse.success("Lấy tủ thuốc gia đình thành công", response);
    }

    @PostMapping("/cabinets/{id}/medicines")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CabinetMedicineResponse> addMedicine(
            @PathVariable("id") Long id,
            @Valid @RequestBody CabinetMedicineRequest request) {
        CabinetMedicineResponse response = medicineCabinetService.addMedicine(id, request);
        return ApiResponse.success("Đã thêm thuốc vào tủ thuốc", response);
    }

    @PutMapping("/cabinets/{id}/medicines/{medicineId}")
    public ApiResponse<CabinetMedicineResponse> updateMedicine(
            @PathVariable("id") Long id,
            @PathVariable("medicineId") Long medicineId,
            @Valid @RequestBody CabinetMedicineUpdateRequest request) {
        CabinetMedicineResponse response = medicineCabinetService.updateMedicine(id, medicineId, request);
        return ApiResponse.success("Cập nhật thuốc thành công", response);
    }

    @DeleteMapping("/cabinets/{id}/medicines/{medicineId}")
    public ApiResponse<Void> removeMedicine(
            @PathVariable("id") Long id,
            @PathVariable("medicineId") Long medicineId) {
        medicineCabinetService.removeMedicine(id, medicineId);
        return ApiResponse.success("Đã xóa thuốc khỏi tủ thuốc", null);
    }

    @GetMapping("/cabinets/{id}/medicines")
    public ApiResponse<List<CabinetMedicineResponse>> getMedicines(
            @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "all") String status) {
        List<CabinetMedicineResponse> responses = medicineCabinetService.getMedicines(id, status);
        return ApiResponse.success("Lấy danh sách thuốc thành công", responses);
    }
}
