package com.example.carenest.feature.chat.data.repository

import com.example.carenest.feature.chat.data.remote.ChatSocketEvent
import com.example.carenest.feature.chat.data.remote.ChatWebSocketClient
import com.example.carenest.feature.chat.domain.model.ChatMessage
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.community.domain.model.GroupPost
import com.google.gson.Gson
import java.time.Instant

sealed interface ChatRepositoryEvent {
    data object Connected : ChatRepositoryEvent
    data class Disconnected(val message: String) : ChatRepositoryEvent
    data class MessageReceived(val message: ChatMessage) : ChatRepositoryEvent
}

class ChatRepository(
    private val api: CommunityApi,
    private val webSocketClient: ChatWebSocketClient,
    private val gson: Gson = Gson()
) {
    suspend fun loadHistory(groupId: Long): List<ChatMessage> {
        val response = api.posts(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải lịch sử tin nhắn")
        }
        return response.body()?.data?.content.orEmpty().map { it.toChatMessage(false) }
    }

    fun connect(groupId: Long, onEvent: (ChatRepositoryEvent) -> Unit) {
        webSocketClient.connect(groupId) { event ->
            when (event) {
                ChatSocketEvent.Connected -> onEvent(ChatRepositoryEvent.Connected)
                is ChatSocketEvent.Disconnected -> onEvent(ChatRepositoryEvent.Disconnected(event.message))
                is ChatSocketEvent.MessageReceived -> onEvent(ChatRepositoryEvent.MessageReceived(parseIncomingMessage(event.payload)))
            }
        }
    }

    fun send(groupId: Long, content: String, onError: (Throwable) -> Unit): Boolean {
        return webSocketClient.send(groupId, gson.toJson(mapOf("content" to content)), onError)
    }

    fun disconnect() = webSocketClient.disconnect()

    private fun parseIncomingMessage(payload: String): ChatMessage {
        return runCatching { gson.fromJson(payload, GroupPost::class.java).toChatMessage(false) }
            .getOrElse {
                ChatMessage(
                    id = "raw-${System.currentTimeMillis()}",
                    text = payload,
                    isMe = false,
                    senderName = "Thành viên",
                    timestamp = System.currentTimeMillis()
                )
            }
    }

    private fun GroupPost.toChatMessage(isMe: Boolean): ChatMessage {
        return ChatMessage(
            id = id.toString(),
            text = content,
            isMe = isMe,
            senderName = authorName ?: "Thành viên",
            senderRole = authorRole,
            replyPreview = replyToPostId?.let { "Đang trả lời một tin nhắn" },
            timestamp = createdAt.toEpochMillisOrNow()
        )
    }

    private fun String?.toEpochMillisOrNow(): Long {
        if (this.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(this).toEpochMilli() }.getOrDefault(System.currentTimeMillis())
    }
}
