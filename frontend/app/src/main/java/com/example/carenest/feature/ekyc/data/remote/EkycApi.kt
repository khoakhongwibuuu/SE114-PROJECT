package com.example.carenest.feature.ekyc.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.ekyc.domain.model.DoctorVerificationResponse
import com.example.carenest.feature.ekyc.domain.model.SubmitDoctorVerificationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EkycApi {
    @GET("/api/v1/doctor-verifications/me")
    suspend fun getMyVerificationStatus(): Response<ApiResponse<DoctorVerificationResponse>>

    @POST("/api/v1/doctor-verifications")
    suspend fun submitVerification(
        @Body request: SubmitDoctorVerificationRequest
    ): Response<ApiResponse<DoctorVerificationResponse>>
}
