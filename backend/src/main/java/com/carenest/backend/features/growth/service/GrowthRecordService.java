package com.carenest.backend.features.growth.service;

import com.carenest.backend.features.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.features.growth.dto.response.GrowthChartResponse;
import com.carenest.backend.features.growth.dto.response.GrowthRecordResponse;

import java.util.List;

public interface GrowthRecordService {
    GrowthRecordResponse addRecord(Long profileId, GrowthRecordCreateRequest request);
    List<GrowthRecordResponse> getGrowthRecords(Long profileId);
    List<GrowthChartResponse> getGrowthChartData(Long profileId);
}
