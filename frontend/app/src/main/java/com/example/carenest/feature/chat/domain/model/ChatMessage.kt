package com.example.carenest.feature.chat.domain.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val senderName: String,
    val senderRole: String? = null,
    val replyPreview: String? = null,
    val timestamp: Long
)
