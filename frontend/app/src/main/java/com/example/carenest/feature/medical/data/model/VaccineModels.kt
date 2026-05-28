package com.example.carenest.feature.medical.data.model

import com.google.gson.annotations.SerializedName

data class RawVaccinationResponse(
    @SerializedName("vaccineName") val vaccineName: String,
    @SerializedName("totalDoses") val totalDoses: Int,
    @SerializedName("healthProfileId") val healthProfileId: Long,
    @SerializedName("doses") val doses: List<RawVaccinationDose> = emptyList()
)

data class RawVaccinationDose(
    @SerializedName("id") val id: Long,
    @SerializedName("doseNumber") val doseNumber: Int,
    @SerializedName("dateAdministered") val dateAdministered: String?,
    @SerializedName("scheduledDate") val scheduledDate: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("status") val status: String? // "COMPLETED" or "PENDING"
)

data class CreateVaccinationRequest(
    @SerializedName("vaccineName") val vaccineName: String,
    @SerializedName("doseNumber") val doseNumber: Int,
    @SerializedName("status") val status: String,
    @SerializedName("date") val date: String,
    @SerializedName("location") val location: String? = null,
    @SerializedName("notes") val notes: String? = null
)

// UI Models
data class VaccinationTrackerGroup(
    val stageLabel: String,
    val description: String,
    val vaccinations: List<VaccinationItem>
)

data class VaccinationItem(
    val id: Long,
    val profileId: Long,
    val fullName: String,
    val vaccineName: String,
    val doseNumber: Int,
    val dateGiven: String?,
    val plannedDate: String?,
    val clinicName: String?,
    val status: String // "DONE" or "PENDING"
)
