package com.example.carenest.feature.notifications.domain.model

data class NotificationItem(
    val id: Long = 0L,
    val userId: Long = 0L,
    val title: String = "",
    val message: String = "",
    val type: String = "SYSTEM",
    val referenceType: String? = null,
    val referenceId: Long? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null
)
