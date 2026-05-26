package com.example.carenest.feature.medical.domain.model

enum class MedicineStatus {
    NORMAL,
    EXPIRING_SOON,
    EXPIRED,
    OUT_OF_STOCK
}

data class Medicine(
    val id: String,
    val name: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: String,
    val status: MedicineStatus,
    val usageNotes: String? = null
)
