package com.example.carenest.model

data class DashboardResponse(
    val families: List<Family> = emptyList(),
    val members: List<Member> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val vaccines: List<Vaccine> = emptyList()
)

data class Family(
    val id: String, 
    val name: String
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
