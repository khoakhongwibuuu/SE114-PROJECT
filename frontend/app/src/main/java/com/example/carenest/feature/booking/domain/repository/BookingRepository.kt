package com.example.carenest.feature.booking.domain.repository

import com.example.carenest.feature.booking.data.remote.BookingApi
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.ConsultationThreadInboxResponse
import com.example.carenest.feature.booking.domain.model.CreateBookingRequest
import com.example.carenest.feature.booking.domain.model.RejectBookingRequest
import com.example.carenest.feature.booking.domain.model.ActiveConsultationDto
import com.example.carenest.feature.booking.domain.model.DuplicateActiveConsultationException
import com.example.carenest.core.data.network.ApiResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookingRepository @Inject constructor(
    private val api: BookingApi
) {
    suspend fun createBookingRequest(request: CreateBookingRequest): Result<BookingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createBookingRequest(request)
                if (response.success) {
                    Result.success(response.data!!)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to create booking request"))
                }
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 409) {
                    try {
                        val errorBody = e.response()?.errorBody()?.string()
                        if (errorBody != null) {
                            val type = object : TypeToken<ApiResponse<ActiveConsultationDto>>() {}.type
                            val errorResponse: ApiResponse<ActiveConsultationDto> = Gson().fromJson(errorBody, type)
                            val activeData = errorResponse.data
                            if (activeData?.code == "DUPLICATE_CONSULTATION") {
                                return@withContext Result.failure(
                                    DuplicateActiveConsultationException(
                                        message = errorResponse.message ?: "Bạn đang có phiên tư vấn mở với bác sĩ này.",
                                        existingBookingId = activeData.existingBookingId,
                                        status = activeData.status
                                    )
                                )
                            }
                        }
                    } catch (parseEx: Exception) {
                        // fallback to default error
                    }
                }
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getDoctorBookings(): Result<List<BookingResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getDoctorBookings()
                if (response.success) {
                    Result.success(response.data ?: emptyList())
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get bookings"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPatientBookings(): Result<List<BookingResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPatientBookings()
                if (response.success) {
                    Result.success(response.data ?: emptyList())
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get bookings"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getConsultationInbox(): Result<List<ConsultationThreadInboxResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getConsultationInbox()
                if (response.success) {
                    Result.success(response.data ?: emptyList())
                } else {
                    Result.failure(Exception(response.message ?: "Failed to get consultation inbox"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun approveBooking(id: Long): Result<BookingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.approveBooking(id)
                if (response.success) {
                    Result.success(response.data!!)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to approve booking"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun rejectBooking(id: Long, reason: String): Result<BookingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.rejectBooking(id, RejectBookingRequest(reason))
                if (response.success) {
                    Result.success(response.data!!)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to reject booking"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun completeConsultation(id: Long): Result<BookingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.completeConsultation(id)
                if (response.success) {
                    Result.success(response.data!!)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to complete consultation"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun restrictMessaging(id: Long): Result<BookingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.restrictMessaging(id)
                if (response.success) {
                    Result.success(response.data!!)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to restrict messaging"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun unrestrictMessaging(id: Long): Result<BookingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.unrestrictMessaging(id)
                if (response.success) {
                    Result.success(response.data!!)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to unrestrict messaging"))
                }
            } catch (e: retrofit2.HttpException) {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    if (errorBody != null) {
                        val type = object : com.google.gson.reflect.TypeToken<com.example.carenest.core.data.network.ApiResponse<Any>>() {}.type
                        val errorResponse: com.example.carenest.core.data.network.ApiResponse<Any> = com.google.gson.Gson().fromJson(errorBody, type)
                        return@withContext Result.failure(Exception(errorResponse.message ?: "Lỗi máy chủ"))
                    }
                } catch (parseEx: Exception) {}
                Result.failure(Exception("Lỗi máy chủ: HTTP ${e.code()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun provisionConsultationThread(bookingId: Long): Result<com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.provisionConsultationThread(bookingId)
                if (response.success) {
                    Result.success(response.data!!)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to provision consultation thread"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getConsultationMessages(threadId: Long): Result<List<com.example.carenest.feature.booking.domain.model.ConsultationMessage>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getConsultationMessages(threadId)
                if (response.success) {
                    Result.success(response.data ?: emptyList())
                } else {
                    Result.failure(Exception(response.message ?: "Failed to fetch consultation messages"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
