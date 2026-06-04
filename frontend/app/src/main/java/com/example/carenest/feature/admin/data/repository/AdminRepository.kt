package com.example.carenest.feature.admin.data.repository

import com.example.carenest.feature.admin.data.AdminApi
import com.example.carenest.feature.admin.data.AdminDashboardStatsResponse
import com.example.carenest.feature.admin.data.AdminReportSummaryResponse
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateRequest
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateResponse
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse

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

    suspend fun getPendingReports(page: Int, size: Int): List<AdminReportSummaryResponse> {
        val response = api.getReports(status = "PENDING", page = page, size = size)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách báo cáo")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun deletePost(postId: Long) {
        val response = api.deletePost(postId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể xóa bài viết vi phạm")
        }
    }

    suspend fun deleteComment(commentId: Long) {
        val response = api.deleteComment(commentId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể xóa bình luận vi phạm")
        }
    }

    suspend fun dismissReport(reportId: Long) {
        val response = api.dismissReport(reportId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể bỏ qua báo cáo")
        }
    }
}
