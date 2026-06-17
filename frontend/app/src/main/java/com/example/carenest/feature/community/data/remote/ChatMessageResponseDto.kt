package com.example.carenest.feature.community.data.remote

data class ChatMessageResponseDto(
    val id: Long,
    val text: String,
    val user: com.example.carenest.feature.community.domain.model.GroupMember,
    val createdAt: String
)
