package com.example.carenest.feature.ekyc.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.ekyc.domain.model.DoctorVerificationResponse
import com.example.carenest.feature.ekyc.domain.model.SubmitDoctorVerificationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import com.example.carenest.feature.ekyc.domain.model.RejectVerificationRequest

interface EkycApi {
    @GET("/api/v1/doctor-verifications/me")
    suspend fun getMyVerificationStatus(): Response<ApiResponse<DoctorVerificationResponse>>

    @POST("/api/v1/doctor-verifications")
    suspend fun submitVerification(
        @Body request: SubmitDoctorVerificationRequest
    ): Response<ApiResponse<DoctorVerificationResponse>>

    @GET("/api/v1/admin/doctor-verifications/pending")
    suspend fun getPendingVerifications(): Response<ApiResponse<List<DoctorVerificationResponse>>>

    @PATCH("/api/v1/admin/doctor-verifications/{id}/approve")
    suspend fun approveVerification(
        @Path("id") id: Long
    ): Response<ApiResponse<DoctorVerificationResponse>>

    @PATCH("/api/v1/admin/doctor-verifications/{id}/reject")
    suspend fun rejectVerification(
        @Path("id") id: Long,
        @Body request: RejectVerificationRequest
    ): Response<ApiResponse<DoctorVerificationResponse>>

    @GET("/api/v1/admin/doctor-verifications/doctors")
    suspend fun getAllDoctors(): Response<ApiResponse<List<DoctorSummary>>>

    @PATCH("/api/v1/admin/doctor-verifications/doctors/{userId}/revoke")
    suspend fun revokeDoctor(
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>
}
