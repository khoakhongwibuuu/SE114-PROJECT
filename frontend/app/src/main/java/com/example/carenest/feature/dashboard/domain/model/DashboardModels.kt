package com.example.carenest.feature.dashboard.domain.model

data class DashboardResponse(
    val unreadNotifications: Long = 0,
    val todayTasks: List<DashboardTask> = emptyList(),
    val families: List<Family> = emptyList(),
    val members: List<Member> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val vaccines: List<Vaccine> = emptyList()
)

data class DashboardTask(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val type: String? = null,
    val dueTime: String? = null
)

data class Family(
    val id: String,
    val name: String,
    val role: String = "Thành viên",
    val memberCount: Int = 0
)

data class Member(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

data class Medication(
    val id: String,
    val name: String,
    val time: String,
    val isTaken: Boolean = false
)

data class Appointment(
    val id: String,
    val doctorName: String,
    val date: String,
    val note: String? = null
)

data class Vaccine(
    val id: String,
    val name: String,
    val date: String,
    val isCompleted: Boolean = false
)
