package com.example.carenest.feature.medical.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.medical.data.model.CreateAppointmentRequest
import com.example.carenest.feature.medical.data.model.RawAppointmentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AppointmentApi {
    @GET("/api/v1/health-profiles/{profileId}/appointments")
    suspend fun getAppointments(@Path("profileId") profileId: Long): Response<ApiResponse<List<RawAppointmentResponse>>>

    @POST("/api/v1/appointments")
    suspend fun createAppointment(@Body request: CreateAppointmentRequest): Response<ApiResponse<Unit>>
}
