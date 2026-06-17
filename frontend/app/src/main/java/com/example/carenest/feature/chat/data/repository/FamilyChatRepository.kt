package com.example.carenest.feature.chat.data.repository

import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData

import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.chat.data.remote.ChatSocketEvent
import com.example.carenest.feature.chat.data.remote.FamilyChatWebSocketClient
import com.example.carenest.feature.chat.domain.model.ChatMessage
import com.example.carenest.feature.family.data.remote.FamilyApi
import com.example.carenest.feature.family.domain.model.FamilyChatMessageResponse
import com.example.carenest.feature.family.domain.model.FamilyChatPageResponse
import com.google.gson.Gson
import java.time.Instant
import java.util.UUID

class FamilyChatRepository(
    private val api: FamilyApi,
    private val webSocketClient: FamilyChatWebSocketClient,
    private val secureSessionManager: SecureSessionManager,
    private val gson: Gson = Gson()
) {
    suspend fun loadHistory(familyId: Long, page: Int = 0, size: Int = 20): FamilyChatPageResponse {
        val response = api.getChatHistory(familyId, page, size)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.errorMessage("Không thể tải lịch sử tin nhắn gia đình"))
        }
        return response.requireData(
            fallback = "Không thể tải lịch sử tin nhắn gia đình",
            missingDataMessage = "Không nhận được phản hồi từ server"
        )
    }

    fun mapToChatMessages(pageResponse: FamilyChatPageResponse): List<ChatMessage> {
        val currentUserId = secureSessionManager.getUserId()
        return pageResponse.content.map { it.toChatMessage(currentUserId) }
    }

    fun connect(familyId: Long, onEvent: (ChatRepositoryEvent) -> Unit) {
        webSocketClient.connect(familyId) { event ->
            when (event) {
                ChatSocketEvent.Connected -> onEvent(ChatRepositoryEvent.Connected)
                is ChatSocketEvent.Disconnected -> {
                    onEvent(ChatRepositoryEvent.Disconnected(event.message))
                }
                is ChatSocketEvent.MessageReceived -> {
                    onEvent(ChatRepositoryEvent.MessageReceived(parseIncomingMessage(event.payload)))
                }
            }
        }
    }

    fun send(familyId: Long, content: String, onError: (Throwable) -> Unit): Boolean {
        val payloadMap = mapOf(
            "familyId" to familyId,
            "content" to content
        )
        val jsonPayload = gson.toJson(payloadMap)
        return webSocketClient.send(jsonPayload, onError)
    }

    fun disconnect() = webSocketClient.disconnect()

    private fun parseIncomingMessage(payload: String): ChatMessage {
        val currentUserId = secureSessionManager.getUserId()
        return runCatching {
            gson.fromJson(payload, FamilyChatMessageResponse::class.java).toChatMessage(currentUserId)
        }.getOrElse {
            ChatMessage(
                id = "raw-${UUID.randomUUID()}",
                text = payload,
                isMe = false,
                senderName = "Thành viên",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun FamilyChatMessageResponse.toChatMessage(currentUserId: Long?): ChatMessage {
        val isMe = (user.id == currentUserId)
        return ChatMessage(
            id = id.toString(),
            text = text,
            isMe = isMe,
            senderName = user.name,
            senderId = user.id,
            senderRole = "MEMBER",
            replyPreview = null,
            timestamp = createdAt.toEpochMillisOrNow()
        )
    }

    private fun String?.toEpochMillisOrNow(): Long {
        if (this.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(this).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())
    }
}
