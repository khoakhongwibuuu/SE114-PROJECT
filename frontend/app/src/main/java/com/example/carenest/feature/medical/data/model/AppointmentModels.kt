package com.example.carenest.feature.medical.data.model

import com.google.gson.annotations.SerializedName

data class RawAppointmentResponse(
    val id: Long,
    val healthProfileId: Long,
    val doctorName: String,
    val hospitalName: String?,
    val address: String?,
    val appointmentDate: String,
    val status: String,
    val notes: String?,
    val resultNotes: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class CreateAppointmentRequest(
    val healthProfileId: Long,
    val hospitalName: String,
    val doctorName: String,
    val appointmentDate: String,
    val address: String?,
    val notes: String?
)

data class AppointmentOverview(
    val upcomingCount: Int,
    val upcomingAppointments: List<AppointmentItem.Upcoming>,
    val appointmentHistory: List<AppointmentItem.History>
)

sealed class AppointmentItem {
    abstract val id: Long
    abstract val title: String
    abstract val appointmentDate: String
    abstract val status: String

    data class Upcoming(
        override val id: Long,
        override val title: String,
        override val appointmentDate: String,
        override val status: String,
        val doctorName: String,
        val location: String?,
        val dayOfWeek: String,
        val dayOfMonth: Int
    ) : AppointmentItem()

    data class History(
        override val id: Long,
        override val title: String,
        override val appointmentDate: String,
        override val status: String,
        val displayDate: String
    ) : AppointmentItem()
}
