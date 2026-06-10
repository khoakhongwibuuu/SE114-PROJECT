package com.example.carenest.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Onboarding : NavKey
@Serializable data object Register : NavKey
@Serializable data object ForgotPassword : NavKey
@Serializable data object MainDashboard : NavKey
@Serializable data object AdminMain : NavKey
@Serializable data object AddMedicine : NavKey
@Serializable data object MedicineSchedule : NavKey
@Serializable data object AddMedicineSchedule : NavKey
@Serializable data object OcrScanner : NavKey
// Appointment & Vaccine (remote team keys)
@Serializable data object AppointmentSchedule : NavKey
@Serializable data object VaccineSchedule : NavKey
@Serializable data class AddVaccine(val profileId: Long, val editVaccineId: Long? = null) : NavKey
// Our completed vaccination module keys
@Serializable data class MedicalAppointment(val profileId: Long) : NavKey
@Serializable data class AddAppointment(val profileId: Long) : NavKey
@Serializable data class VaccinationTracker(val profileId: Long) : NavKey
@Serializable data class AddVaccinationSchedule(val profileId: Long, val vaccineId: Long? = null, val doseId: Long? = null) : NavKey

// Module 8 Route Keys
@Serializable data object NotificationsCenter : NavKey
@Serializable data object DoctorVerification : NavKey
@Serializable data class UserMedical(val profileId: Long) : NavKey
@Serializable data object Policy : NavKey
@Serializable data object PatientBookingCenter : NavKey

@Serializable data class ChatRoom(val id: Long, val name: String) : NavKey
@Serializable data class FamilyChatRoom(val id: Long, val name: String, val memberCount: Int) : NavKey


@Serializable data class GroupPostDetail(val groupId: Long, val groupName: String) : NavKey
@Serializable data class CreateGroupPost(val groupId: Long) : NavKey

@Serializable data class DoctorProfile(val doctorId: Long) : NavKey

@Serializable data object DoctorWorkspace : NavKey

@Serializable data class ConsultationRoom(val bookingId: Long) : NavKey
