package com.example.carenest.feature.booking.domain.port

import com.example.carenest.feature.booking.data.remote.ConsultationSocketEvent
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.ConsultationMessage
import com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary

interface BookingDataSource {
    suspend fun getDoctors(): List<DoctorSummary>
    suspend fun getMyBookings(): List<BookingResponse>
    suspend fun getDoctorBookings(): List<BookingResponse>
    suspend fun approveBooking(bookingId: Long): Result<BookingResponse>
    suspend fun confirmSchedule(
        bookingId: Long,
        scheduledAtIso: String,
        confirmedLocation: String?,
        confirmedNote: String?
    ): BookingResponse
    suspend fun rejectBooking(
        bookingId: Long,
        rejectionReason: String
    ): BookingResponse
    suspend fun provisionConsultationThread(bookingId: Long): Result<ConsultationThreadResponse>
    suspend fun getConsultationMessages(threadId: Long): Result<List<ConsultationMessage>>
    suspend fun completeConsultation(bookingId: Long): Result<BookingResponse>
    suspend fun restrictMessaging(bookingId: Long): Result<BookingResponse>
    suspend fun unrestrictMessaging(bookingId: Long): Result<BookingResponse>
}

interface ConsultationSocketGateway {
    fun connect(threadId: Long, onEvent: (ConsultationSocketEvent) -> Unit)
    fun send(threadId: Long, payload: String, onError: (Throwable) -> Unit): Boolean
    fun disconnect()
}
