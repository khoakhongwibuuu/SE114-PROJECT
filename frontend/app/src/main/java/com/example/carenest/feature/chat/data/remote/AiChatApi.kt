package com.example.carenest.feature.chat.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AiChatRequest(
    val message: String,
    val conversationId: Int? = null
)

data class AiChatReply(
    val reply: String,
    val id: Int,
    val message_id: Int,
    val conversation_id: Int
)

interface AiChatApi {
    @POST("/ai/chat")
    suspend fun chatWithAi(@Body request: AiChatRequest): Response<AiChatReply>
}
