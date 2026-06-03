package com.example.carenest.feature.admin.data.repository

import com.example.carenest.feature.admin.data.AdminApi
import com.example.carenest.feature.admin.data.AdminDashboardStatsResponse
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateResponse
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateRequest

class AdminRepository(
    private val api: AdminApi,
) {
    suspend fun getDashboardStats(): AdminDashboardStatsResponse {
        val response = api.getDashboardStats()
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải thống kê quản trị")
        }
        return response.body()?.data ?: throw IllegalStateException("Không nhận được dữ liệu thống kê")
    }

    suspend fun getUsers(page: Int, size: Int, search: String?): List<AdminUserSummaryResponse> {
        val response = api.getUsers(page = page, size = size, search = search?.takeIf { it.isNotBlank() })
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách người dùng")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun updateUserStatus(userId: Long, status: String): AdminUserStatusUpdateResponse {
        val response = api.updateUserStatus(
            userId = userId,
            request = AdminUserStatusUpdateRequest(status = status),
        )
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể cập nhật trạng thái người dùng")
        }
        return response.body()?.data ?: throw IllegalStateException("Không nhận được trạng thái mới")
    }
}
