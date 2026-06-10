package com.example.carenest.feature.booking.domain.model

data class ConsultationMessage(
    val id: Long,
    val roomId: Long,
    val senderId: Long,
    val senderName: String,
    val senderAvatarUrl: String?,
    val content: String,
    val createdAt: String
)

data class SendConsultationMessageRequest(
    val content: String
)
