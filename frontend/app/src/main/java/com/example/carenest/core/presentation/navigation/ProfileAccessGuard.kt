package com.example.carenest.core.presentation.navigation

const val MISSING_HEALTH_PROFILE_MESSAGE = "Vui lòng chọn hoặc tạo hồ sơ sức khỏe trước."

fun Long?.isValidHealthProfileId(): Boolean = this != null && this > 0L
