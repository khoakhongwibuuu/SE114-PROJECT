package com.example.carenest.feature.booking.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.ConsultationThreadInboxResponse
import com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse
import com.example.carenest.feature.booking.domain.model.CreateBookingRequest
import com.example.carenest.feature.booking.domain.model.RejectBookingRequest
import com.example.carenest.feature.booking.domain.model.ConsultationMessage
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BookingApi {

    @POST("api/v1/bookings")
    suspend fun createBookingRequest(
        @Body request: CreateBookingRequest
    ): ApiResponse<BookingResponse>

    @GET("api/v1/bookings/doctor")
    suspend fun getDoctorBookings(): ApiResponse<List<BookingResponse>>

    @GET("api/v1/bookings/patient")
    suspend fun getPatientBookings(): ApiResponse<List<BookingResponse>>

    @GET("api/v1/bookings/consultation-inbox")
    suspend fun getConsultationInbox(): ApiResponse<List<ConsultationThreadInboxResponse>>

    @POST("api/v1/bookings/{id}/approve")
    suspend fun approveBooking(
        @Path("id") id: Long
    ): ApiResponse<BookingResponse>

    @POST("/api/v1/bookings/{id}/reject")
    suspend fun rejectBooking(
        @Path("id") id: Long,
        @Body request: RejectBookingRequest
    ): ApiResponse<BookingResponse>

    @POST("/api/v1/bookings/{id}/complete")
    suspend fun completeConsultation(
        @Path("id") id: Long
    ): ApiResponse<BookingResponse>

    @POST("/api/v1/bookings/{id}/restrict")
    suspend fun restrictMessaging(
        @Path("id") id: Long
    ): ApiResponse<BookingResponse>

    @POST("/api/v1/bookings/{id}/unrestrict")
    suspend fun unrestrictMessaging(
        @Path("id") id: Long
    ): ApiResponse<BookingResponse>

    @POST("/api/v1/bookings/{id}/consultation-thread")
    suspend fun provisionConsultationThread(
        @Path("id") id: Long
    ): ApiResponse<ConsultationThreadResponse>

    @GET("/api/v1/consultations/threads/{threadId}/messages")
    suspend fun getConsultationMessages(
        @Path("threadId") threadId: Long
    ): ApiResponse<List<ConsultationMessage>>
}
