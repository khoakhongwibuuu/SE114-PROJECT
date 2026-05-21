package com.carenest.backend.module.growth.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.module.growth.dto.response.GrowthChartResponse;
import com.carenest.backend.module.growth.dto.response.GrowthRecordResponse;
import com.carenest.backend.module.growth.service.GrowthRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health-profiles/{id}")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class GrowthRecordController {

    private final GrowthRecordService growthRecordService;

    @PostMapping("/growth-records")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GrowthRecordResponse> addGrowthRecord(
            @PathVariable("id") Long id,
            @Valid @RequestBody GrowthRecordCreateRequest request) {
        
        GrowthRecordResponse response = growthRecordService.addRecord(id, request);
        return ApiResponse.success("Thêm chỉ số tăng trưởng thành công", response);
    }

    @GetMapping("/growth-records")
    public ApiResponse<List<GrowthRecordResponse>> getGrowthRecords(
            @PathVariable("id") Long id) {
        
        List<GrowthRecordResponse> response = growthRecordService.getGrowthRecords(id);
        return ApiResponse.success("Lấy lịch sử tăng trưởng thành công", response);
    }

    @GetMapping("/growth-chart")
    public ApiResponse<List<GrowthChartResponse>> getGrowthChartData(
            @PathVariable("id") Long id) {
        
        List<GrowthChartResponse> response = growthRecordService.getGrowthChartData(id);
        return ApiResponse.success("Lấy dữ liệu biểu đồ tăng trưởng thành công", response);
    }
}
