package com.example.carenest.feature.chat.data.repository

import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.chat.data.remote.ChatSocketEvent
import com.example.carenest.feature.chat.data.remote.ChatWebSocketClient
import com.example.carenest.feature.chat.domain.model.ChatGroupPreview
import com.example.carenest.feature.chat.domain.model.ChatMessage
import com.example.carenest.feature.community.data.remote.ChatMessageResponseDto
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.community.data.remote.ReportPostRequest
import com.example.carenest.feature.community.data.remote.SendGroupMessageRequest
import com.google.gson.Gson
import java.time.Instant
import java.util.UUID

sealed interface ChatRepositoryEvent {
    data object Connected : ChatRepositoryEvent
    data class Disconnected(val message: String) : ChatRepositoryEvent
    data class MessageReceived(val message: ChatMessage) : ChatRepositoryEvent
}

class ChatRepository(
    private val api: CommunityApi,
    private val webSocketClient: ChatWebSocketClient,
    private val secureSessionManager: SecureSessionManager,
    private val gson: Gson = Gson()
) {
    suspend fun loadHistory(groupId: Long): List<ChatMessage> {
        val response = api.groupMessages(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.errorMessage("Không thể tải lịch sử tin nhắn"))
        }

        val currentUserId = secureSessionManager.getUserId()
        return response.requireData("Không thể tải lịch sử tin nhắn").content.map { it.toChatMessage(currentUserId) }
    }

    suspend fun loadGroupPreview(groupId: Long): ChatGroupPreview {
        val response = api.preview(groupId)
        return response.requireData("Không thể tải thông tin nhóm", "Không nhận được thông tin nhóm")
    }

    suspend fun sendViaRest(groupId: Long, content: String): ChatMessage {
        val response = api.sendGroupMessage(groupId, SendGroupMessageRequest(content = content))
        val message = response.requireData("Không thể gửi tin nhắn", "Không nhận được tin nhắn mới")
        return message.toChatMessage(secureSessionManager.getUserId())
    }

    fun connect(groupId: Long, onEvent: (ChatRepositoryEvent) -> Unit) {
        webSocketClient.connect(groupId) { event ->
            when (event) {
                ChatSocketEvent.Connected -> onEvent(ChatRepositoryEvent.Connected)
                is ChatSocketEvent.Disconnected -> onEvent(ChatRepositoryEvent.Disconnected(event.message))
                is ChatSocketEvent.MessageReceived -> {
                    onEvent(ChatRepositoryEvent.MessageReceived(parseIncomingMessage(event.payload)))
                }
            }
        }
    }

    fun sendOverSocket(groupId: Long, content: String, onError: (Throwable) -> Unit): Boolean {
        val payload = gson.toJson(SendGroupMessageRequest(content = content))
        return webSocketClient.send(groupId, payload, onError)
    }

    fun disconnect() = webSocketClient.disconnect()

    suspend fun leaveGroup(groupId: Long) {
        val response = api.leave(groupId)
        response.requireSuccess("Không thể rời nhóm")
    }

    suspend fun kickMember(groupId: Long, userId: Long, reason: String) {
        val response = api.kickMember(groupId, userId, reason)
        response.requireSuccess("Không thể mời thành viên rời nhóm")
    }

    suspend fun reportMessage(messageId: Long, reason: String) {
        val response = api.reportGroupMessage(messageId, ReportPostRequest(reason))
        response.requireSuccess("Không thể báo cáo tin nhắn")
    }

    private fun parseIncomingMessage(payload: String): ChatMessage {
        val currentUserId = secureSessionManager.getUserId()
        return runCatching { gson.fromJson(payload, ChatMessageResponseDto::class.java).toChatMessage(currentUserId) }
            .getOrElse {
                ChatMessage(
                    id = "raw-${UUID.randomUUID()}",
                    text = payload,
                    isMe = false,
                    senderName = "Thành viên",
                    timestamp = System.currentTimeMillis()
                )
            }
    }

    private fun ChatMessageResponseDto.toChatMessage(currentUserId: Long?): ChatMessage {
        return ChatMessage(
            id = id?.toString() ?: "remote-${UUID.randomUUID()}",
            text = text.orEmpty(),
            isMe = user?.userId != null && user.userId == currentUserId,
            senderName = user?.name ?: "Thành viên",
            senderId = user?.userId,
            senderRole = user?.role,
            timestamp = createdAt.toEpochMillisOrNow()
        )
    }

    private fun String?.toEpochMillisOrNow(): Long {
        if (this.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(this).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())
    }
}
