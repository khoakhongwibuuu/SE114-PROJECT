package com.example.carenest.feature.medical.data.remote

import com.example.carenest.feature.medical.data.model.CreateVaccinationRequest
import com.example.carenest.feature.medical.data.model.RawVaccinationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VaccineApi {
    @GET("api/v1/health-profiles/{profileId}/vaccinations")
    suspend fun getVaccinationTracker(
        @Path("profileId") profileId: Long
    ): List<RawVaccinationResponse>

    @POST("api/v1/health-profiles/{profileId}/vaccinations")
    suspend fun createVaccination(
        @Path("profileId") profileId: Long,
        @Body request: CreateVaccinationRequest
    )
}
