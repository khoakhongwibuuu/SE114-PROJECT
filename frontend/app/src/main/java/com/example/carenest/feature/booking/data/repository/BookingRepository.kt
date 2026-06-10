package com.example.carenest.feature.booking.data.repository

import com.example.carenest.feature.booking.data.remote.BookingApi
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.data.remote.CancelBookingRequest
import com.example.carenest.feature.booking.data.remote.ConfirmBookingScheduleRequest
import com.example.carenest.feature.booking.domain.model.CreateBookingRequest
import com.example.carenest.feature.booking.domain.model.RejectBookingRequest
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary

class BookingRepository(
    private val bookingApi: BookingApi
) {
    suspend fun getDoctors(): List<DoctorSummary> {
        val response = bookingApi.getDoctors()
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải danh sách bác sĩ")
        }
        return response.body()?.data.orEmpty()
    }

    suspend fun getMyBookings(): List<BookingResponse> {
        val response = bookingApi.getPatientBookings()
        return response.data.orEmpty()
    }

    suspend fun getDoctorBookings(): List<BookingResponse> {
        val response = bookingApi.getDoctorBookings()
        return response.data.orEmpty()
    }

    suspend fun createBooking(
        doctorId: Long,
        healthProfileId: Long,
        type: BookingRequestType,
        preferredSchedule: String?,
        patientNote: String?
    ): BookingResponse {
        val response = bookingApi.createBookingRequest(
            CreateBookingRequest(
                doctorId = doctorId,
                healthProfileId = healthProfileId,
                requestType = type,
                preferredSchedule = preferredSchedule?.takeIf { it.isNotBlank() },
                note = patientNote?.takeIf { it.isNotBlank() } ?: ""
            )
        )
        return response.data ?: throw IllegalStateException(response.message ?: "Thiếu dữ liệu phản hồi khi tạo yêu cầu")
    }

    suspend fun confirmSchedule(
        bookingId: Long,
        scheduledAtIso: String,
        confirmedLocation: String?,
        confirmedNote: String?
    ): BookingResponse {
        val response = bookingApi.confirmSchedule(
            id = bookingId,
            request = ConfirmBookingScheduleRequest(
                scheduledAt = scheduledAtIso,
                confirmedLocation = confirmedLocation?.takeIf { it.isNotBlank() },
                confirmedNote = confirmedNote?.takeIf { it.isNotBlank() }
            )
        )
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể xác nhận lịch")
        }
        return response.body()?.data ?: throw IllegalStateException("Thiếu dữ liệu phản hồi khi xác nhận lịch")
    }

    suspend fun rejectBooking(
        bookingId: Long,
        rejectionReason: String
    ): BookingResponse {
        val response = bookingApi.rejectBooking(
            id = bookingId,
            request = RejectBookingRequest(rejectionReason.trim())
        )
        return response.data ?: throw IllegalStateException(response.message ?: "Thiếu dữ liệu phản hồi khi từ chối")
    }

    suspend fun cancelBooking(
        bookingId: Long,
        cancellationReason: String?
    ): BookingResponse {
        val response = bookingApi.cancelBooking(
            id = bookingId,
            request = CancelBookingRequest(cancellationReason?.takeIf { it.isNotBlank() })
        )
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể hủy yêu cầu")
        }
        return response.body()?.data ?: throw IllegalStateException("Thiếu dữ liệu phản hồi khi hủy")
    }

    suspend fun getConsultationInbox(): Result<List<com.example.carenest.feature.booking.domain.model.ConsultationThreadInboxResponse>> {
        return try {
            val response = bookingApi.getConsultationInbox()
            Result.success(response.data.orEmpty())
        } catch (e: Exception) {
            Result.failure(extractErrorMessage(e))
        }
    }

    suspend fun approveBooking(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.approveBooking(bookingId)
            Result.success(response.data ?: throw java.lang.IllegalStateException(response.message ?: "Thiếu dữ liệu"))
        } catch (e: Exception) {
            Result.failure(extractErrorMessage(e))
        }
    }

    suspend fun provisionConsultationThread(bookingId: Long): Result<com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse> {
        return try {
            val response = bookingApi.provisionConsultationThread(bookingId)
            Result.success(response.data ?: throw java.lang.IllegalStateException(response.message ?: "Thiếu dữ liệu"))
        } catch (e: Exception) {
            Result.failure(extractErrorMessage(e))
        }
    }

    suspend fun getConsultationMessages(threadId: Long): Result<List<com.example.carenest.feature.booking.domain.model.ConsultationMessage>> {
        return try {
            val response = bookingApi.getConsultationMessages(threadId)
            Result.success(response.data.orEmpty())
        } catch (e: Exception) {
            Result.failure(extractErrorMessage(e))
        }
    }

    suspend fun completeConsultation(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.completeConsultation(bookingId)
            Result.success(response.data ?: throw java.lang.IllegalStateException(response.message ?: "Thiếu dữ liệu"))
        } catch (e: Exception) {
            Result.failure(extractErrorMessage(e))
        }
    }

    suspend fun restrictMessaging(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.restrictMessaging(bookingId)
            Result.success(response.data ?: throw java.lang.IllegalStateException(response.message ?: "Thiếu dữ liệu"))
        } catch (e: Exception) {
            Result.failure(extractErrorMessage(e))
        }
    }

    suspend fun unrestrictMessaging(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.unrestrictMessaging(bookingId)
            Result.success(response.data ?: throw java.lang.IllegalStateException(response.message ?: "Thiếu dữ liệu"))
        } catch (e: Exception) {
            Result.failure(extractErrorMessage(e))
        }
    }

    private fun extractErrorMessage(e: Exception): Exception {
        if (e is retrofit2.HttpException) {
            return try {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    val json = org.json.JSONObject(errorBody)
                    val message = json.optString("message", "Lỗi từ máy chủ: ${e.code()}")
                    Exception(message)
                } else {
                    Exception("HTTP ${e.code()} - ${e.message()}")
                }
            } catch (ex: Exception) {
                Exception("HTTP ${e.code()} - ${e.message()}")
            }
        }
        return e
    }
}
