package com.carenest.backend.features.dashboard.service;

import com.carenest.backend.features.dashboard.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardOverview(Long familyId, Long profileId);
}
