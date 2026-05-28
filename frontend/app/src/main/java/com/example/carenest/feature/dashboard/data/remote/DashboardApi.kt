package com.example.carenest.feature.dashboard.data.remote

import com.example.carenest.feature.dashboard.domain.model.DashboardResponse
import com.example.carenest.core.data.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {
    @GET("/api/v1/dashboard")
    suspend fun getDashboard(): Response<ApiResponse<DashboardResponse>>
}
