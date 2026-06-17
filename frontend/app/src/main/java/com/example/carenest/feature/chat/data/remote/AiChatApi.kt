package com.example.carenest.feature.chat.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AiChatRequest(
    val message: String,
    val conversationId: Int? = null
)

data class AiChatAction(
    val type: String,
    val label: String
)

data class AiChatSafety(
    val needs_doctor: Boolean,
    val needs_emergency: Boolean,
    val disclaimer: String
)

data class AiChatStructuredPayload(
    val intent: String,
    val summary: String,
    val advice: List<String> = emptyList(),
    val risk_level: String,
    val follow_up_questions: List<String> = emptyList(),
    val recommended_actions: List<AiChatAction> = emptyList(),
    val safety: AiChatSafety
)

data class AiChatReply(
    val reply: String,
    val id: Int,
    val message_id: Int? = null,
    val conversation_id: Int? = null,
    val structured: AiChatStructuredPayload? = null
)

interface AiChatApi {
    @POST("/ai/chat")
    suspend fun chatWithAi(@Body request: AiChatRequest): Response<AiChatReply>
}
