package com.example.carenest.feature.chat.domain.model

data class ChatGroup(
    val id: Long,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val private: Boolean = false,
    val leadDoctorId: Long? = null,
    val leadDoctorName: String? = null,
    val memberCount: Long = 0,
    val joined: Boolean = false,
    val latestMessage: String? = null,
    val latestActivityAt: String? = null,
    val isFrozen: Boolean = false
)

data class ChatGroupPreview(
    val id: Long,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val private: Boolean = false,
    val leadDoctorId: Long? = null,
    val leadDoctorName: String? = null,
    val memberCount: Long = 0,
    val joined: Boolean = false,
    val myRole: String? = null,
    val rules: String? = null,
    val isFrozen: Boolean = false
)
