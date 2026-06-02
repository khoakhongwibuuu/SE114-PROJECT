package com.example.carenest.feature.admin.data

import com.example.carenest.core.data.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

data class AdminDashboardStatsResponse(
    val totalUsers: Long = 0,
    val totalDoctors: Long = 0,
    val pendingEkycCount: Long = 0,
    val moderationQueueCount: Long = 0,
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

interface AdminApi {
    @GET("/api/v1/admin/dashboard-stats")
    suspend fun getDashboardStats(): Response<ApiResponse<AdminDashboardStatsResponse>>

    @GET("/api/v1/admin/users")
    suspend fun getUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<ApiResponse<List<AdminUserSummaryResponse>>>

    @PATCH("/api/v1/admin/users/{userId}/status")
    suspend fun updateUserStatus(
        @Path("userId") userId: Long,
        @Body request: AdminUserStatusUpdateRequest,
    ): Response<ApiResponse<AdminUserStatusUpdateResponse>>
}
