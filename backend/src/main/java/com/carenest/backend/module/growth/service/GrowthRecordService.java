package com.carenest.backend.module.growth.service;

import com.carenest.backend.module.growth.dto.request.GrowthRecordCreateRequest;
import com.carenest.backend.module.growth.dto.response.GrowthChartResponse;
import com.carenest.backend.module.growth.dto.response.GrowthRecordResponse;

import java.util.List;

public interface GrowthRecordService {
    GrowthRecordResponse addRecord(Long profileId, GrowthRecordCreateRequest request);
    List<GrowthRecordResponse> getGrowthRecords(Long profileId);
    List<GrowthChartResponse> getGrowthChartData(Long profileId);
}
