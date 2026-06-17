package com.example.carenest.feature.admin.data.repository

import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.feature.admin.data.AdminApi
import com.example.carenest.feature.admin.data.AdminDashboardStatsResponse
import com.example.carenest.feature.admin.data.AdminReportSummaryResponse
import com.example.carenest.feature.admin.data.AdminUserRoleUpdateRequest
import com.example.carenest.feature.admin.data.AdminUserRoleUpdateResponse
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateRequest
import com.example.carenest.feature.admin.data.AdminUserStatusUpdateResponse
import com.example.carenest.feature.admin.data.AdminUserSummaryResponse

class AdminRepository(
    private val api: AdminApi,
) {
    suspend fun getDashboardStats(): AdminDashboardStatsResponse {
        val response = api.getDashboardStats()
        return response.requireData("Không thể tải thống kê quản trị", "Không nhận được dữ liệu thống kê")
    }

    suspend fun getUsers(page: Int, size: Int, search: String?): List<AdminUserSummaryResponse> {
        val response = api.getUsers(page = page, size = size, search = search?.takeIf { it.isNotBlank() })
        return response.requireData("Không thể tải danh sách người dùng").content
    }

    suspend fun updateUserStatus(userId: Long, status: String): AdminUserStatusUpdateResponse {
        val response = api.updateUserStatus(
            userId = userId,
            request = AdminUserStatusUpdateRequest(status = status),
        )
        return response.requireData("Không thể cập nhật trạng thái người dùng", "Không nhận được trạng thái mới")
    }

    suspend fun updateUserRole(userId: Long, role: String): AdminUserRoleUpdateResponse {
        val response = api.updateUserRole(
            userId = userId,
            request = AdminUserRoleUpdateRequest(role = role),
        )
        return response.requireData("Không thể cập nhật quyền người dùng", "Không nhận được quyền mới")
    }

    suspend fun getPendingReports(page: Int, size: Int): List<AdminReportSummaryResponse> {
        val response = api.getReports(status = "PENDING", page = page, size = size)
        return response.requireList("Không thể tải danh sách báo cáo")
    }

    suspend fun deletePost(postId: Long) {
        val response = api.deletePost(postId)
        response.requireSuccess("Không thể xóa bài viết vi phạm")
    }

    suspend fun deleteComment(commentId: Long) {
        val response = api.deleteComment(commentId)
        response.requireSuccess("Không thể xóa bình luận vi phạm")
    }

    suspend fun deleteMessage(messageId: Long) {
        val response = api.deleteMessage(messageId)
        response.requireSuccess("Không thể xóa tin nhắn vi phạm")
    }

    suspend fun dismissReport(reportId: Long) {
        val response = api.dismissReport(reportId)
        response.requireSuccess("Không thể bỏ qua báo cáo")
    }
}
