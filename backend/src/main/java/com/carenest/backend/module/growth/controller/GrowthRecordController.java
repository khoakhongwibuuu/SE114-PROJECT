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
public class GrowthRecordController {

    private final GrowthRecordService growthRecordService;

    @PostMapping("/growth-records")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GrowthRecordResponse> addGrowthRecord(
            @PathVariable Long id,
            @Valid @RequestBody GrowthRecordCreateRequest request) {
        
        GrowthRecordResponse response = growthRecordService.addRecord(id, request);
        return ApiResponse.success("Added growth record successfully", response);
    }

    @GetMapping("/growth-records")
    public ApiResponse<List<GrowthRecordResponse>> getGrowthRecords(
            @PathVariable Long id) {
        
        List<GrowthRecordResponse> response = growthRecordService.getGrowthRecords(id);
        return ApiResponse.success("Fetched growth records successfully", response);
    }

    @GetMapping("/growth-chart")
    public ApiResponse<List<GrowthChartResponse>> getGrowthChartData(
            @PathVariable Long id) {
        
        List<GrowthChartResponse> response = growthRecordService.getGrowthChartData(id);
        return ApiResponse.success("Fetched growth chart data successfully", response);
    }
}
