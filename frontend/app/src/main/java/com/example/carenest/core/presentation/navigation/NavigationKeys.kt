package com.example.carenest.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Onboarding : NavKey
@Serializable data object Register : NavKey
@Serializable data object MainDashboard : NavKey
@Serializable data object AddMedicine : NavKey
@Serializable data object FamilyPicker : NavKey
@Serializable data class FamilyManagement(val mode: String? = null) : NavKey
@Serializable data object FamilyList : NavKey
@Serializable data class ChatRoom(val familyId: Int, val familyName: String) : NavKey
@Serializable data class HealthProfileDetail(val memberId: Int) : NavKey
@Serializable data object MedicineSchedule : NavKey
@Serializable data object AddMedicineSchedule : NavKey
@Serializable data object OcrScanner : NavKey
@Serializable data object AppointmentSchedule : NavKey
@Serializable data object VaccineSchedule : NavKey
@Serializable data class AddVaccine(val profileId: Long, val editVaccineId: Long? = null) : NavKey
