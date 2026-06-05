package com.carenest.backend.features.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsResponse {
    private long totalUsers;
    private long totalDoctors;
    private long pendingEkycCount;
    private long moderationQueueCount;
    private List<Long> trend;
}
