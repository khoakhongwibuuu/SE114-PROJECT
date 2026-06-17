package com.example.carenest.feature.admin.data

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.core.data.network.PageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

data class AdminDashboardStatsResponse(
    val totalUsers: Long = 0,
    val totalDoctors: Long = 0,
    val pendingEkycCount: Long = 0,
    val moderationQueueCount: Long = 0,
    val trend: List<Long> = emptyList(),
)

data class AdminUserSummaryResponse(
    val id: Long,
    val email: String,
    val fullName: String? = null,
    val role: String = "USER",
    val status: String = "ACTIVE",
)

data class AdminUserStatusUpdateRequest(
    val status: String,
)

data class AdminUserStatusUpdateResponse(
    val id: Long,
    val status: String,
)

data class AdminUserRoleUpdateRequest(
    val role: String,
)

data class AdminUserRoleUpdateResponse(
    val id: Long,
    val role: String,
)

data class AdminReportSummaryResponse(
    val id: Long,
    val postId: Long? = null,
    val messageId: Long? = null,
    val commentId: Long? = null,
    val contentType: String = "POST",
    val reporterId: Long? = null,
    val reporterName: String? = null,
    val reporterEmail: String? = null,
    val reason: String,
    val previewText: String? = null,
    val previewImageUrl: String? = null,
    val contentAuthorName: String? = null,
    val status: String = "PENDING",
    val createdAt: String? = null,
) {
    fun normalizedContentType(): AdminContentType {
        return when (contentType.trim().uppercase()) {
            "COMMENT" -> AdminContentType.COMMENT
            "MESSAGE" -> AdminContentType.MESSAGE
            else -> AdminContentType.POST
        }
    }
}

enum class AdminContentType {
    POST,
    COMMENT,
    MESSAGE,
}

interface AdminApi {
    @GET("/api/v1/admin/dashboard-stats")
    suspend fun getDashboardStats(): Response<ApiResponse<AdminDashboardStatsResponse>>

    @GET("/api/v1/admin/users")
    suspend fun getUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
    ): Response<ApiResponse<PageResponse<AdminUserSummaryResponse>>>

    @PATCH("/api/v1/admin/users/{userId}/status")
    suspend fun updateUserStatus(
        @Path("userId") userId: Long,
        @Body request: AdminUserStatusUpdateRequest,
    ): Response<ApiResponse<AdminUserStatusUpdateResponse>>

    @PATCH("/api/v1/admin/users/{userId}/role")
    suspend fun updateUserRole(
        @Path("userId") userId: Long,
        @Body request: AdminUserRoleUpdateRequest,
    ): Response<ApiResponse<AdminUserRoleUpdateResponse>>

    @GET("/api/v1/admin/reports")
    suspend fun getReports(
        @Query("status") status: String = "PENDING",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ApiResponse<List<AdminReportSummaryResponse>>>

    @DELETE("/api/v1/admin/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Long,
    ): Response<ApiResponse<Unit>>

    @DELETE("/api/v1/admin/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long,
    ): Response<ApiResponse<Unit>>

    @DELETE("/api/v1/admin/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: Long,
    ): Response<ApiResponse<Unit>>

    @PATCH("/api/v1/admin/reports/{reportId}/dismiss")
    suspend fun dismissReport(
        @Path("reportId") reportId: Long,
    ): Response<ApiResponse<Unit>>
}
