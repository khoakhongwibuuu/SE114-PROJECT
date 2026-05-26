package com.example.carenest

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Register : NavKey
@Serializable data object MainDashboard : NavKey
@Serializable data object AddMedicine : NavKey

@Serializable data object FamilyPicker : NavKey
@Serializable data class FamilyManagement(val mode: String? = null) : NavKey
@Serializable data object FamilyList : NavKey
@Serializable data class ChatRoom(val familyId: Int, val familyName: String) : NavKey
@Serializable data class HealthProfileDetail(val memberId: Int) : NavKey
