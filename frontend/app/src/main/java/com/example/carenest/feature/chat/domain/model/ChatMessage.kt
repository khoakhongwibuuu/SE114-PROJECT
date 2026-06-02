package com.example.carenest.feature.chat.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val senderName: String,
    val senderId: Long? = null,
    val senderRole: String? = null,
    val replyPreview: String? = null,
    val timestamp: Long
)
