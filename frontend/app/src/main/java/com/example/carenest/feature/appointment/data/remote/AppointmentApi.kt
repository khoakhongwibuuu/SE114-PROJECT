package com.example.carenest.feature.appointment.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class AppointmentResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("healthProfileId") val healthProfileId: Long,
    @SerializedName("doctorName") val doctorName: String,
    @SerializedName("hospitalName") val hospitalName: String,
    @SerializedName("address") val address: String?,
    @SerializedName("appointmentDate") val appointmentDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("resultNotes") val resultNotes: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class CreateAppointmentRequest(
    @SerializedName("healthProfileId") val healthProfileId: Long,
    @SerializedName("hospitalName") val hospitalName: String,
    @SerializedName("doctorName") val doctorName: String,
    @SerializedName("appointmentDate") val appointmentDate: String,
    @SerializedName("address") val address: String?,
    @SerializedName("notes") val notes: String?
)

interface AppointmentApi {
    @GET("/api/v1/health-profiles/{profileId}/appointments")
    suspend fun getAppointments(
        @Path("profileId") profileId: Long
    ): Response<ApiResponse<List<AppointmentResponse>>>

    @POST("/api/v1/appointments")
    suspend fun createAppointment(
        @Body request: CreateAppointmentRequest
    ): Response<ApiResponse<AppointmentResponse>>

    @PUT("/api/v1/appointments/{appointmentId}/cancel")
    suspend fun cancelAppointment(
        @Path("appointmentId") appointmentId: Long
    ): Response<ApiResponse<AppointmentResponse>>
}
