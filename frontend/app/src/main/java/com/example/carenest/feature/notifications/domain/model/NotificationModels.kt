package com.example.carenest.feature.notifications.domain.model

data class NotificationItem(
    val notificationId: Long,
    val userId: Long,
    val profileId: Long?,
    val title: String,
    val content: String,
    val type: String,
    val isRead: Boolean,
    val referenceId: Long?,
    val scheduledTime: String?,
    val createdAt: String?,
    val updatedAt: String?
)
