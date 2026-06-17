package com.example.carenest.feature.notifications.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.core.data.network.PageResponse
import com.example.carenest.feature.notifications.domain.model.NotificationItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("/api/v1/notifications")
    suspend fun getNotifications(
        @Query("type") type: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<PageResponse<NotificationItem>>>

    @PATCH("/api/v1/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Long
    ): Response<ApiResponse<NotificationItem>>

    @PATCH("/api/v1/notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponse<UnreadCountResponse>>

    @GET("/api/v1/notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponse>>
}

data class UnreadCountResponse(
    val count: Long = 0
)
