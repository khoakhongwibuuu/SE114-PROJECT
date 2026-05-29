package com.example.carenest.feature.dashboard.data.remote

import com.example.carenest.feature.dashboard.domain.model.DashboardResponse
import com.example.carenest.core.data.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DashboardApi {
    @GET("/api/v1/dashboard")
    suspend fun getDashboard(
        @Query("familyId") familyId: String? = null
    ): Response<ApiResponse<DashboardResponse>>
}
