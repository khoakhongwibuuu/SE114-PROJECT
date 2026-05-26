package com.example.carenest.network

import com.example.carenest.model.DashboardResponse
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApi {
    @GET("/api/v1/dashboard")
    suspend fun getDashboard(): Response<DashboardResponse>
}
