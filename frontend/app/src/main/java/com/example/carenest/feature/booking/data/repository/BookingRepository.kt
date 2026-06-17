package com.example.carenest.feature.booking.data.repository

import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.core.data.network.userException
import com.example.carenest.feature.booking.data.remote.BookingApi
import com.example.carenest.feature.booking.data.remote.CancelBookingRequest
import com.example.carenest.feature.booking.data.remote.ConfirmBookingScheduleRequest
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.CreateBookingRequest
import com.example.carenest.feature.booking.domain.model.DuplicateActiveConsultationException
import com.example.carenest.feature.booking.domain.model.RejectBookingRequest
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import org.json.JSONObject

class BookingRepository(
    private val bookingApi: BookingApi
) {
    suspend fun getDoctors(): List<DoctorSummary> {
        val response = bookingApi.getDoctors()
        return response.requireList("Không thể tải danh sách bác sĩ")
    }

    suspend fun getMyBookings(): List<BookingResponse> {
        val response = bookingApi.getPatientBookings()
        return response.requireList("Không thể tải lịch sử đặt khám")
    }

    suspend fun getDoctorBookings(): List<BookingResponse> {
        val response = bookingApi.getDoctorBookings()
        return response.requireList("Không thể tải danh sách yêu cầu của bác sĩ")
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
                preferredTimeNote = preferredSchedule?.takeIf { it.isNotBlank() },
                note = patientNote?.takeIf { it.isNotBlank() } ?: ""
            )
        )
        if (!response.isSuccessful) {
            val rawError = runCatching { response.errorBody()?.string() }.getOrNull()
            parseDuplicateConsultation(rawError)?.let { throw it }
            throw IllegalStateException(parseErrorMessage(rawError) ?: "Không thể gửi yêu cầu đặt lịch (${response.code()})")
        }
        val body = response.body()
        return body?.data ?: throw IllegalStateException(body?.message ?: "Thiếu dữ liệu phản hồi khi tạo yêu cầu")
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
        return response.requireData(
            "Không thể xác nhận lịch",
            "Thiếu dữ liệu phản hồi khi xác nhận lịch"
        )
    }

    suspend fun rejectBooking(
        bookingId: Long,
        rejectionReason: String
    ): BookingResponse {
        val response = bookingApi.rejectBooking(
            id = bookingId,
            request = RejectBookingRequest(rejectionReason.trim())
        )
        return response.requireData("Không thể từ chối yêu cầu")
    }

    suspend fun cancelBooking(
        bookingId: Long,
        cancellationReason: String?
    ): BookingResponse {
        val response = bookingApi.cancelBooking(
            id = bookingId,
            request = CancelBookingRequest(cancellationReason?.takeIf { it.isNotBlank() })
        )
        return response.requireData(
            "Không thể hủy yêu cầu",
            "Thiếu dữ liệu phản hồi khi hủy yêu cầu"
        )
    }

    suspend fun getConsultationInbox(): Result<List<com.example.carenest.feature.booking.domain.model.ConsultationThreadInboxResponse>> {
        return try {
            val response = bookingApi.getConsultationInbox()
            Result.success(response.requireList("Không thể tải danh sách tư vấn"))
        } catch (e: Exception) {
            Result.failure(e.userException("Không thể tải danh sách tư vấn"))
        }
    }

    suspend fun approveBooking(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.approveBooking(bookingId)
            Result.success(response.requireData("Không thể chấp nhận yêu cầu"))
        } catch (e: Exception) {
            Result.failure(e.userException("Không thể chấp nhận yêu cầu"))
        }
    }

    suspend fun provisionConsultationThread(bookingId: Long): Result<com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse> {
        return try {
            val response = bookingApi.provisionConsultationThread(bookingId)
            Result.success(response.requireData("Không thể mở phòng tư vấn"))
        } catch (e: Exception) {
            Result.failure(e.userException("Không thể mở phòng tư vấn"))
        }
    }

    suspend fun getConsultationMessages(threadId: Long): Result<List<com.example.carenest.feature.booking.domain.model.ConsultationMessage>> {
        return try {
            val response = bookingApi.getConsultationMessages(threadId)
            Result.success(response.requireList("Không thể tải tin nhắn tư vấn"))
        } catch (e: Exception) {
            Result.failure(e.userException("Không thể tải tin nhắn tư vấn"))
        }
    }

    suspend fun completeConsultation(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.completeConsultation(bookingId)
            Result.success(response.requireData("Không thể kết thúc phiên tư vấn"))
        } catch (e: Exception) {
            Result.failure(e.userException("Không thể kết thúc phiên tư vấn"))
        }
    }

    suspend fun restrictMessaging(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.restrictMessaging(bookingId)
            Result.success(response.requireData("Không thể hạn chế nhắn tin"))
        } catch (e: Exception) {
            Result.failure(e.userException("Không thể hạn chế nhắn tin"))
        }
    }

    suspend fun unrestrictMessaging(bookingId: Long): Result<BookingResponse> {
        return try {
            val response = bookingApi.unrestrictMessaging(bookingId)
            Result.success(response.requireData("Không thể bỏ hạn chế nhắn tin"))
        } catch (e: Exception) {
            Result.failure(e.userException("Không thể bỏ hạn chế nhắn tin"))
        }
    }
}

private fun parseDuplicateConsultation(rawError: String?): DuplicateActiveConsultationException? {
    if (rawError.isNullOrBlank()) return null
    return runCatching {
        val json = JSONObject(rawError)
        val data = json.optJSONObject("data") ?: return@runCatching null
        if (data.optString("code") != "DUPLICATE_CONSULTATION") return@runCatching null
        val existingBookingId = data.optLong("existingBookingId", -1L).takeIf { it > 0L }
            ?: return@runCatching null
        DuplicateActiveConsultationException(
            message = json.optString("message").takeIf { it.isNotBlank() }
                ?: "Bạn đang có một yêu cầu hoặc phiên tư vấn đang hoạt động với bác sĩ này.",
            existingBookingId = existingBookingId,
            status = data.optString("status").takeIf { it.isNotBlank() } ?: "ACTIVE"
        )
    }.getOrNull()
}

private fun parseErrorMessage(rawError: String?): String? {
    if (rawError.isNullOrBlank()) return null
    return runCatching {
        val json = JSONObject(rawError)
        sequenceOf(
            json.optString("message"),
            json.optString("error"),
            json.optString("detail")
        ).firstOrNull { it.isNotBlank() }
    }.getOrNull()
}
