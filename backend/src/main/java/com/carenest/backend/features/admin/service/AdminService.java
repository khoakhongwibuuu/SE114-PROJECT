package com.carenest.backend.features.admin.service;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.admin.dto.request.AdminUserStatusUpdateRequest;
import com.carenest.backend.features.admin.dto.response.AdminDashboardStatsResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserStatusUpdateResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserSummaryResponse;

public interface AdminService {
    AdminDashboardStatsResponse getDashboardStats();
    PageResponse<AdminUserSummaryResponse> getUsers(int page, int size, String search);
    AdminUserStatusUpdateResponse updateUserStatus(Long userId, AdminUserStatusUpdateRequest request);
}
