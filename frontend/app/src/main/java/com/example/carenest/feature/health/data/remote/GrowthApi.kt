package com.example.carenest.feature.health.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.health.domain.model.GrowthChartPointResponse
import com.example.carenest.feature.health.domain.model.GrowthRecordCreateRequest
import com.example.carenest.feature.health.domain.model.GrowthRecordResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GrowthApi {
    @POST("/api/v1/health-profiles/{profileId}/growth-records")
    suspend fun addGrowthRecord(
        @Path("profileId") profileId: Long,
        @Body request: GrowthRecordCreateRequest
    ): Response<ApiResponse<GrowthRecordResponse>>

    @GET("/api/v1/health-profiles/{profileId}/growth-records")
    suspend fun getGrowthRecords(
        @Path("profileId") profileId: Long
    ): Response<ApiResponse<List<GrowthRecordResponse>>>

    @GET("/api/v1/health-profiles/{profileId}/growth-chart")
    suspend fun getGrowthChart(
        @Path("profileId") profileId: Long
    ): Response<ApiResponse<List<GrowthChartPointResponse>>>
}
