package com.example.carenest.feature.booking.presentation.doctorworkspace

import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus
import com.example.carenest.feature.booking.domain.model.ConsultationMessage
import com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse
import com.example.carenest.feature.booking.domain.port.BookingDataSource
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DoctorWorkspaceViewModelTest {

    @Test
    fun prioritizeDoctorWorkspaceBookings_keepsSeparateBookingChannelsForSamePatient() {
        val online = booking(
            id = 1L,
            patientId = 10L,
            type = BookingRequestType.ONLINE_CHAT,
            status = BookingStatus.APPROVED
        )
        val offline = booking(
            id = 2L,
            patientId = 10L,
            type = BookingRequestType.OFFLINE_CLINIC,
            status = BookingStatus.PENDING
        )

        val result = prioritizeDoctorWorkspaceBookings(listOf(online, offline))

        assertEquals(listOf(online.id, offline.id), result.map { it.id })
    }

    @Test
    fun prioritizeDoctorWorkspaceBookings_sortsRestrictedBeforeCompleted() {
        val completed = booking(
            id = 1L,
            patientId = 10L,
            type = BookingRequestType.ONLINE_CHAT,
            status = BookingStatus.COMPLETED,
            createdAt = "2026-06-14T10:00:00Z"
        )
        val restricted = booking(
            id = 2L,
            patientId = 11L,
            type = BookingRequestType.ONLINE_CHAT,
            status = BookingStatus.RESTRICTED,
            createdAt = "2026-06-14T09:00:00Z"
        )

        val result = prioritizeDoctorWorkspaceBookings(listOf(completed, restricted))

        assertEquals(listOf(restricted.id, completed.id), result.map { it.id })
    }

    @Test
    fun prioritizeDoctorWorkspaceBookings_keepsHighestPriorityWithinPatientChannel() {
        val completed = booking(
            id = 1L,
            patientId = 10L,
            type = BookingRequestType.ONLINE_CHAT,
            status = BookingStatus.COMPLETED,
            createdAt = "2026-06-14T11:00:00Z"
        )
        val pending = booking(
            id = 2L,
            patientId = 10L,
            type = BookingRequestType.ONLINE_CHAT,
            status = BookingStatus.PENDING,
            createdAt = "2026-06-14T10:00:00Z"
        )

        val result = prioritizeDoctorWorkspaceBookings(listOf(completed, pending))

        assertEquals(listOf(pending.id), result.map { it.id })
    }

    @Test
    fun approveBooking_updatesBookingAndClearsBusyState() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val pending = booking(
                id = 5L,
                patientId = 10L,
                type = BookingRequestType.ONLINE_CHAT,
                status = BookingStatus.PENDING
            )
            val approved = pending.copy(status = BookingStatus.APPROVED)
            val repository = FakeDoctorWorkspaceBookingDataSource(
                doctorBookings = listOf(pending),
                approveResult = Result.success(approved)
            )
            val viewModel = DoctorWorkspaceViewModel(repository)
            advanceUntilIdle()

            var successCalled = false
            viewModel.approveBooking(
                id = 5L,
                onSuccess = { successCalled = true },
                onError = { error("unexpected error callback: $it") }
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(BookingStatus.APPROVED, state.bookings.single().status)
            assertTrue(state.busyBookingIds.isEmpty())
            assertTrue(successCalled)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun rejectBooking_surfacesErrorAndClearsBusyState() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val pending = booking(
                id = 8L,
                patientId = 20L,
                type = BookingRequestType.ONLINE_CHAT,
                status = BookingStatus.PENDING
            )
            val repository = FakeDoctorWorkspaceBookingDataSource(
                doctorBookings = listOf(pending),
                rejectError = IllegalStateException("Không thể từ chối lúc này")
            )
            val viewModel = DoctorWorkspaceViewModel(repository)
            advanceUntilIdle()

            var errorMessage: String? = null
            viewModel.rejectBooking(
                id = 8L,
                reason = "Busy",
                onSuccess = { error("unexpected success callback") },
                onError = { errorMessage = it }
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Không thể từ chối lúc này", errorMessage)
            assertEquals("Không thể từ chối lúc này", state.error)
            assertTrue(state.busyBookingIds.isEmpty())
            assertEquals(BookingStatus.PENDING, state.bookings.single().status)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun booking(
        id: Long,
        patientId: Long,
        type: BookingRequestType,
        status: BookingStatus,
        createdAt: String = "2026-06-14T08:00:00Z"
    ): BookingResponse {
        return BookingResponse(
            id = id,
            patientId = patientId,
            patientFullName = "Patient $patientId",
            patientAvatarUrl = null,
            doctorId = 99L,
            requestType = type,
            status = status,
            note = "Need care",
            createdAt = createdAt
        )
    }
}

private class FakeDoctorWorkspaceBookingDataSource(
    private val doctorBookings: List<BookingResponse> = emptyList(),
    private val approveResult: Result<BookingResponse> = Result.failure(IllegalStateException("approve not configured")),
    private val rejectResult: BookingResponse? = null,
    private val rejectError: Exception? = null,
) : BookingDataSource {
    override suspend fun getDoctors(): List<DoctorSummary> = emptyList()
    override suspend fun getMyBookings(): List<BookingResponse> = emptyList()
    override suspend fun getDoctorBookings(): List<BookingResponse> = doctorBookings
    override suspend fun approveBooking(bookingId: Long): Result<BookingResponse> = approveResult
    override suspend fun confirmSchedule(
        bookingId: Long,
        scheduledAtIso: String,
        confirmedLocation: String?,
        confirmedNote: String?
    ): BookingResponse = error("confirmSchedule not expected")
    override suspend fun rejectBooking(bookingId: Long, rejectionReason: String): BookingResponse {
        rejectError?.let { throw it }
        return rejectResult ?: error("rejectResult not configured")
    }
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
