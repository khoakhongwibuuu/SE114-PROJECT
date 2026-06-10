package com.example.carenest.feature.booking.domain.model

data class CreateBookingRequest(
    val doctorId: Long,
    val requestType: BookingRequestType,
    val note: String,
    val preferredTimeNote: String? = null
)

data class RejectBookingRequest(
    val reason: String
)

data class BookingResponse(
    val id: Long,
    val patientId: Long,
    val patientFullName: String,
    val patientAvatarUrl: String?,
    val doctorId: Long,
    val doctorFullName: String? = null,
    val doctorAvatarUrl: String? = null,
    val requestType: BookingRequestType,
    val status: BookingStatus,
    val note: String,
    val preferredTimeNote: String? = null,
    val rejectReason: String? = null,
    val createdAt: String? = null
)

data class ConsultationThreadResponse(
    val id: Long,
    val bookingRequestId: Long,
    val patientId: Long,
    val patientFullName: String,
    val patientAvatarUrl: String?,
    val doctorId: Long,
    val doctorFullName: String,
    val doctorAvatarUrl: String?,
    val status: BookingStatus? = null
)

data class ConsultationThreadInboxResponse(
    val id: Long,
    val latestBookingId: Long,
    val patientId: Long,
    val patientFullName: String,
    val patientAvatarUrl: String?,
    val doctorId: Long,
    val doctorFullName: String,
    val doctorAvatarUrl: String?,
    val status: BookingStatus
)

class DuplicateActiveConsultationException(
    message: String,
    val existingBookingId: Long,
    val status: String
) : Exception(message)

data class ActiveConsultationDto(
    val code: String,
    val existingBookingId: Long,
    val status: String
)
