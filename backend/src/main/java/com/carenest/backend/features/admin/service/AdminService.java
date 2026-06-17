package com.carenest.backend.features.admin.service;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.admin.dto.request.AdminUserRoleUpdateRequest;
import com.carenest.backend.features.admin.dto.request.AdminUserStatusUpdateRequest;
import com.carenest.backend.features.admin.dto.response.AdminDashboardStatsResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserAuditLogResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserRoleUpdateResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserStatusUpdateResponse;
import com.carenest.backend.features.admin.dto.response.AdminUserSummaryResponse;
import com.carenest.backend.features.auth.entity.User;

import java.util.List;

public interface AdminService {
    AdminDashboardStatsResponse getDashboardStats();
    PageResponse<AdminUserSummaryResponse> getUsers(int page, int size, String search);
    List<AdminUserAuditLogResponse> getRecentUserAuditLogs();
    AdminUserStatusUpdateResponse updateUserStatus(Long userId, AdminUserStatusUpdateRequest request, User currentAdmin);
    AdminUserRoleUpdateResponse updateUserRole(Long userId, AdminUserRoleUpdateRequest request, User currentAdmin);
}
