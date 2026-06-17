package com.example.carenest.feature.booking.presentation.consultation

import com.example.carenest.feature.booking.data.remote.ConsultationSocketEvent
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus
import com.example.carenest.feature.booking.domain.model.ConsultationMessage
import com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse
import com.example.carenest.feature.booking.domain.port.BookingDataSource
import com.example.carenest.feature.booking.domain.port.ConsultationSocketGateway
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsultationRoomViewModelTest {

    @Test
    fun loadRoom_connectsSocketAndDeduplicatesIncomingMessages() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val thread = thread(status = BookingStatus.APPROVED)
            val existingMessage = message(id = 11L, content = "hello")
            val repository = FakeConsultationBookingDataSource(
                provisionResult = Result.success(thread),
                messagesResult = Result.success(listOf(existingMessage))
            )
            val socket = FakeConsultationSocketGateway()
            val viewModel = ConsultationRoomViewModel(repository, socket)

            viewModel.loadRoom(bookingId = 700L)
            advanceUntilIdle()

            socket.emit(ConsultationSocketEvent.Connected)
            socket.emit(ConsultationSocketEvent.MessageReceived(jsonMessage(id = 11L, content = "duplicate")))
            socket.emit(ConsultationSocketEvent.MessageReceived(jsonMessage(id = 12L, content = "new message")))
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(thread.id, state.thread?.id)
            assertTrue(state.isConnected)
            assertEquals(listOf(11L, 12L), state.messages.map { it.id })
            assertEquals(700L, repository.provisionBookingId)
            assertEquals(thread.id, socket.connectedThreadId)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun sendMessage_validatesBlankAndDisconnectedStates() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val thread = thread(status = BookingStatus.ACTIVE)
            val repository = FakeConsultationBookingDataSource(
                provisionResult = Result.success(thread),
                messagesResult = Result.success(emptyList())
            )
            val socket = FakeConsultationSocketGateway()
            val viewModel = ConsultationRoomViewModel(repository, socket)
            viewModel.loadRoom(bookingId = 701L)
            advanceUntilIdle()

            val blankResult = viewModel.sendMessage("   ")
            assertFalse(blankResult)
            assertEquals("Nội dung tin nhắn không được để trống", viewModel.state.value.error)

            viewModel.clearError()
            val disconnectedResult = viewModel.sendMessage("Xin chào")
            assertFalse(disconnectedResult)
            assertEquals("Phòng tư vấn đang mất kết nối, vui lòng thử lại sau", viewModel.state.value.error)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun completeConsultation_updatesThreadStatusAndSuccessMessage() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val thread = thread(status = BookingStatus.ACTIVE)
            val repository = FakeConsultationBookingDataSource(
                provisionResult = Result.success(thread),
                messagesResult = Result.success(emptyList()),
                completeResult = Result.success(booking(status = BookingStatus.COMPLETED))
            )
            val socket = FakeConsultationSocketGateway()
            val viewModel = ConsultationRoomViewModel(repository, socket)
            viewModel.loadRoom(bookingId = 702L)
            advanceUntilIdle()

            var onDoneCalled = false
            viewModel.completeConsultation(702L) { onDoneCalled = true }
            advanceUntilIdle()

            val state = viewModel.state.value
            assertEquals(BookingStatus.COMPLETED, state.thread?.status)
            assertEquals("Phiên tư vấn đã kết thúc.", state.actionSuccess)
            assertTrue(onDoneCalled)
            assertFalse(state.isActionLoading)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun thread(status: BookingStatus): ConsultationThreadResponse {
        return ConsultationThreadResponse(
            id = 900L,
            bookingRequestId = 700L,
            patientId = 10L,
            patientFullName = "Patient",
            patientAvatarUrl = null,
            doctorId = 20L,
            doctorFullName = "Doctor",
            doctorAvatarUrl = null,
            status = status
        )
    }

    private fun booking(status: BookingStatus): BookingResponse {
        return BookingResponse(
            id = 702L,
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

    private fun message(id: Long, content: String): ConsultationMessage {
        return ConsultationMessage(
            id = id,
            roomId = 900L,
            senderId = 20L,
            senderName = "Doctor",
            senderAvatarUrl = null,
            content = content,
            createdAt = "2026-06-18T08:00:00Z"
        )
    }

    private fun jsonMessage(id: Long, content: String): String {
        return """
            {"id":$id,"roomId":900,"senderId":20,"senderName":"Doctor","senderAvatarUrl":null,"content":"$content","createdAt":"2026-06-18T08:00:00Z"}
        """.trimIndent()
    }
}

private class FakeConsultationBookingDataSource(
    private val provisionResult: Result<ConsultationThreadResponse>,
    private val messagesResult: Result<List<ConsultationMessage>>,
    private val completeResult: Result<BookingResponse> = Result.failure(IllegalStateException("complete not configured")),
) : BookingDataSource {
    var provisionBookingId: Long? = null

    override suspend fun getDoctors(): List<DoctorSummary> = emptyList()
    override suspend fun getMyBookings(): List<BookingResponse> = emptyList()
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
    override suspend fun provisionConsultationThread(bookingId: Long): Result<ConsultationThreadResponse> {
        provisionBookingId = bookingId
        return provisionResult
    }
    override suspend fun getConsultationMessages(threadId: Long): Result<List<ConsultationMessage>> = messagesResult
    override suspend fun completeConsultation(bookingId: Long): Result<BookingResponse> = completeResult
    override suspend fun restrictMessaging(bookingId: Long): Result<BookingResponse> =
        error("restrictMessaging not expected")
    override suspend fun unrestrictMessaging(bookingId: Long): Result<BookingResponse> =
        error("unrestrictMessaging not expected")
}

private class FakeConsultationSocketGateway : ConsultationSocketGateway {
    var connectedThreadId: Long? = null
    private var callback: ((ConsultationSocketEvent) -> Unit)? = null

    override fun connect(threadId: Long, onEvent: (ConsultationSocketEvent) -> Unit) {
        connectedThreadId = threadId
        callback = onEvent
    }

    override fun send(threadId: Long, payload: String, onError: (Throwable) -> Unit): Boolean = true

    override fun disconnect() = Unit

    fun emit(event: ConsultationSocketEvent) {
        callback?.invoke(event)
    }
}
