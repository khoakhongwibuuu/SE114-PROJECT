package com.example.carenest.feature.booking.domain.model

data class CreateBookingRequest(
    val doctorId: Long,
    val requestType: BookingRequestType,
    val note: String,
    val preferredTimeNote: String? = null,
    val healthProfileId: Long? = null,
    val preferredSchedule: String? = null
)

data class RejectBookingRequest(
    val rejectionReason: String
)

data class BookingResponse(
    val id: Long,
    val patientId: Long,
    val patientFullName: String,
    val patientAvatarUrl: String?,
    val doctorId: Long,
    val doctorFullName: String? = null,
    val doctorAvatarUrl: String? = null,
    val doctorSpecialty: String? = null,
    val doctorHospitalName: String? = null,
    val healthProfileName: String? = null,
    val requestType: BookingRequestType,
    val status: BookingStatus,
    val note: String,
    val preferredTimeNote: String? = null,
    val rejectReason: String? = null,
    val cancellationReason: String? = null,
    val scheduledAt: String? = null,
    val confirmedLocation: String? = null,
    val confirmedNote: String? = null,
    val appointmentId: Long? = null,
    val healthProfileId: Long? = null,
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
