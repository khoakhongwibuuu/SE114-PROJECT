package com.example.carenest.feature.health.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.health.domain.model.AdministerDoseRequest
import com.example.carenest.feature.health.domain.model.CreateVaccinationRequest
import com.example.carenest.feature.health.domain.model.VaccinationRecordResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VaccinationApi {
    @GET("/api/v1/health-profiles/{profileId}/vaccinations")
    suspend fun getVaccinations(
        @Path("profileId") profileId: Long
    ): Response<ApiResponse<List<VaccinationRecordResponse>>>

    @POST("/api/v1/health-profiles/{profileId}/vaccinations")
    suspend fun createVaccinationPlan(
        @Path("profileId") profileId: Long,
        @Body request: CreateVaccinationRequest
    ): Response<ApiResponse<VaccinationRecordResponse>>

    @PUT("/api/v1/vaccination-doses/{doseId}/administer")
    suspend fun administerDose(
        @Path("doseId") doseId: Long,
        @Body request: AdministerDoseRequest
    ): Response<ApiResponse<VaccinationRecordResponse>>
}
