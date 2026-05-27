package com.example.carenest.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Onboarding : NavKey
@Serializable data object Register : NavKey
@Serializable data object MainDashboard : NavKey
@Serializable data object AddMedicine : NavKey
@Serializable data object MedicineSchedule : NavKey
@Serializable data object AddMedicineSchedule : NavKey
@Serializable data object OcrScanner : NavKey
