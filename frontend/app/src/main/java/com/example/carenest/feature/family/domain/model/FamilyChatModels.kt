package com.example.carenest.feature.family.domain.model

import com.google.gson.annotations.SerializedName

data class FamilyChatMessageResponse(
    @SerializedName("_id") val id: Long,
    val text: String,
    val createdAt: String,
    val user: FamilyChatUserDto
)

data class FamilyChatUserDto(
    @SerializedName("_id") val id: Long,
    val name: String,
    val avatar: String? = null
)

data class FamilyChatPageResponse(
    val content: List<FamilyChatMessageResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)
