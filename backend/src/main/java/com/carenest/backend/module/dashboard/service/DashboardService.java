package com.carenest.backend.module.dashboard.service;

import com.carenest.backend.module.dashboard.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardOverview(Long familyId, Long profileId);
}
