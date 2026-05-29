package com.example.carenest.feature.health.domain.model

data class VaccinationRecordResponse(
    val id: Long,
    val healthProfileId: Long,
    val vaccineName: String,
    val totalDoses: Int,
    val doseIntervalDays: Int?,
    val notes: String?,
    val doses: List<VaccinationDoseResponse>
)

data class VaccinationDoseResponse(
    val id: Long,
    val doseNumber: Int,
    val scheduledDate: String,
    val dateAdministered: String?,
    val location: String?,
    val administeredBy: String?,
    val status: String,
    val notes: String?
)

data class CreateVaccinationRequest(
    val vaccineName: String,
    val doseNumber: Int,
    val status: String,
    val date: String,
    val location: String? = null,
    val notes: String? = null
)

data class AdministerDoseRequest(
    val dateAdministered: String,
    val location: String? = null,
    val administeredBy: String? = null,
    val notes: String? = null
)
