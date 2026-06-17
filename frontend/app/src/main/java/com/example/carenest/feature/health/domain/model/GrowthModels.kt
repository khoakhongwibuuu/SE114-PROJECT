package com.example.carenest.feature.health.domain.model

data class GrowthRecordCreateRequest(
    val recordDate: String,
    val weightKg: Double,
    val heightCm: Double,
    val headCircumferenceCm: Double? = null,
    val notes: String? = null
)

data class GrowthRecordResponse(
    val id: Long,
    val recordDate: String,
    val weightKg: Double,
    val heightCm: Double,
    val headCircumferenceCm: Double? = null,
    val bmi: Double? = null,
    val weightPercentile: Double? = null,
    val heightPercentile: Double? = null,
    val isAnomalous: Boolean? = null,
    val notes: String? = null,
    val createdAt: String? = null
)

data class GrowthChartPointResponse(
    val recordDate: String,
    val weightKg: Double,
    val heightCm: Double,
    val bmi: Double? = null,
    val weightPercentile: Double? = null,
    val heightPercentile: Double? = null
)
