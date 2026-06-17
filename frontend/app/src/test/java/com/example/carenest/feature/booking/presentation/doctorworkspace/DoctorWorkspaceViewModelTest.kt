package com.example.carenest.feature.booking.presentation.doctorworkspace

import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus
import org.junit.Assert.assertEquals
import org.junit.Test

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
