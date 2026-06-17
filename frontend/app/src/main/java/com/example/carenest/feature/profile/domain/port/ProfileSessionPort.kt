package com.example.carenest.feature.profile.domain.port

import kotlinx.coroutines.flow.StateFlow

interface ProfileSessionPort {
    val userRoleFlow: StateFlow<String?>
    fun saveUserIdSync(userId: Long)
    fun saveUserRoleSync(role: String?)
}
