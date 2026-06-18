package com.example.carenest.feature.chat.data.remote

import com.example.carenest.core.data.network.ApiResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AiChatRequest(
    val message: String,
    val conversationId: Long? = null
)

data class AiChatAction(
    val type: String,
    val label: String
)

data class AiChatSafety(
    val needsDoctor: Boolean,
    val needsEmergency: Boolean,
    val disclaimer: String
)

data class AiChatStructuredPayload(
    val intent: String,
    val summary: String,
    val advice: List<String> = emptyList(),
    val riskLevel: String,
    val followUpQuestions: List<String> = emptyList(),
    val recommendedActions: List<AiChatAction> = emptyList(),
    val safety: AiChatSafety
)

data class AiChatReply(
    val reply: String,
    val id: Long? = null,
    @SerializedName("messageId") val messageId: Long? = null,
    @SerializedName("conversationId") val conversationId: Long? = null,
    val structured: AiChatStructuredPayload? = null
)

interface AiChatApi {
    @POST("/api/v1/chat/send")
    suspend fun chatWithAi(@Body request: AiChatRequest): Response<ApiResponse<AiChatReply>>
}
