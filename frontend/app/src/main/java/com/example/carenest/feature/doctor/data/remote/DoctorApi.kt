package com.example.carenest.feature.doctor.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.doctor.domain.model.DoctorPublicProfile
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DoctorApi {
    @GET("/api/v1/doctors/{id}/profile")
    suspend fun getDoctorProfile(@Path("id") id: Long): Response<ApiResponse<DoctorPublicProfile>>
}
