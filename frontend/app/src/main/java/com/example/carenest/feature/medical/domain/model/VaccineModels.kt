package com.example.carenest.feature.medical.domain.model

data class VaccinationItem(
    val id: Long,
    val vaccineName: String,
    val doseNumber: Int,
    val status: String, // "DONE" or "PENDING"
    val dateGiven: String? = null,
    val plannedDate: String? = null,
    val clinicName: String? = null,
    val notes: String? = null
)

data class VaccinationTrackerGroup(
    val stageLabel: String,
    val vaccinations: List<VaccinationItem>
)

data class CreateVaccinationRequest(
    val vaccineName: String,
    val doseNumber: Int,
    val status: String,
    val date: String,
    val location: String? = null,
    val notes: String? = null
)
