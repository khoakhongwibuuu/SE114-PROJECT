package com.example.carenest.feature.booking.presentation.patient

import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus
import com.example.carenest.feature.booking.domain.model.ConsultationMessage
import com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse
import com.example.carenest.feature.booking.domain.port.BookingDataSource
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PatientBookingCenterViewModelTest {

    @Test
    fun loadBookings_keepsBookingHistoryWhenDoctorLookupFails() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val bookings = listOf(
                booking(id = 1L, status = BookingStatus.PENDING),
                booking(id = 2L, status = BookingStatus.COMPLETED)
            )
            val repository = FakePatientBookingDataSource(
                myBookings = bookings,
                doctorsError = IllegalStateException("doctor lookup failed")
            )
            val viewModel = PatientBookingCenterViewModel(repository)

            viewModel.loadBookings()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(bookings, state.bookings)
            assertTrue(state.doctors.isEmpty())
            assertEquals(null, state.error)
            assertEquals(false, state.isLoading)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun loadBookings_surfacesErrorWhenBookingHistoryFails() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = FakePatientBookingDataSource(
                myBookingsError = IllegalStateException("Không thể tải lịch sử")
            )
            val viewModel = PatientBookingCenterViewModel(repository)

            viewModel.loadBookings()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Không thể tải lịch sử", state.error)
            assertTrue(state.bookings.isEmpty())
            assertEquals(false, state.isLoading)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun booking(id: Long, status: BookingStatus): BookingResponse {
        return BookingResponse(
            id = id,
            patientId = 10L,
            patientFullName = "Patient",
            patientAvatarUrl = null,
            doctorId = 20L,
            requestType = BookingRequestType.ONLINE_CHAT,
            status = status,
            note = "Need care",
            createdAt = "2026-06-18T08:00:00Z"
        )
    }
}

private class FakePatientBookingDataSource(
    private val myBookings: List<BookingResponse> = emptyList(),
    private val myBookingsError: Exception? = null,
    private val doctors: List<DoctorSummary> = emptyList(),
    private val doctorsError: Exception? = null,
) : BookingDataSource {
    override suspend fun getDoctors(): List<DoctorSummary> {
        doctorsError?.let { throw it }
        return doctors
    }

    override suspend fun getMyBookings(): List<BookingResponse> {
        myBookingsError?.let { throw it }
        return myBookings
    }

    override suspend fun getDoctorBookings(): List<BookingResponse> = emptyList()
    override suspend fun approveBooking(bookingId: Long): Result<BookingResponse> = error("approveBooking not expected")
    override suspend fun confirmSchedule(
        bookingId: Long,
        scheduledAtIso: String,
        confirmedLocation: String?,
        confirmedNote: String?
    ): BookingResponse = error("confirmSchedule not expected")
    override suspend fun rejectBooking(bookingId: Long, rejectionReason: String): BookingResponse =
        error("rejectBooking not expected")
    override suspend fun provisionConsultationThread(bookingId: Long): Result<ConsultationThreadResponse> =
        error("provisionConsultationThread not expected")
    override suspend fun getConsultationMessages(threadId: Long): Result<List<ConsultationMessage>> =
        error("getConsultationMessages not expected")
    override suspend fun completeConsultation(bookingId: Long): Result<BookingResponse> =
        error("completeConsultation not expected")
    override suspend fun restrictMessaging(bookingId: Long): Result<BookingResponse> =
        error("restrictMessaging not expected")
    override suspend fun unrestrictMessaging(bookingId: Long): Result<BookingResponse> =
        error("unrestrictMessaging not expected")
}
