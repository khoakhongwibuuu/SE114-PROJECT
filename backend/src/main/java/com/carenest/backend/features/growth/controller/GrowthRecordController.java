package com.carenest.backend.features.growth.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.features.growth.dto.response.GrowthChartResponse;
import com.carenest.backend.features.growth.dto.response.GrowthRecordResponse;
import com.carenest.backend.features.growth.service.GrowthRecordService;
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
        return ApiResponse.success("ThÃªm chá»‰ sá»‘ tÄƒng trÆ°á»Ÿng thÃ nh cÃ´ng", response);
    }

    @GetMapping("/growth-records")
    public ApiResponse<List<GrowthRecordResponse>> getGrowthRecords(
            @PathVariable("id") Long id) {

        List<GrowthRecordResponse> response = growthRecordService.getGrowthRecords(id);
        return ApiResponse.success("Láº¥y lá»‹ch sá»­ tÄƒng trÆ°á»Ÿng thÃ nh cÃ´ng", response);
    }

    @GetMapping("/growth-chart")
    public ApiResponse<List<GrowthChartResponse>> getGrowthChartData(
            @PathVariable("id") Long id) {

        List<GrowthChartResponse> response = growthRecordService.getGrowthChartData(id);
        return ApiResponse.success("Láº¥y dá»¯ liá»‡u biá»ƒu Ä‘á»“ tÄƒng trÆ°á»Ÿng thÃ nh cÃ´ng", response);
    }
}
