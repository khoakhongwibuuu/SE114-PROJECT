package com.example.carenest.feature.chat.data.repository

import com.example.carenest.core.data.storage.SecureSessionManager
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
    private val secureSessionManager: SecureSessionManager,
    private val gson: Gson = Gson(),
) {
    suspend fun loadHistory(groupId: Long): List<ChatMessage> {
        val response = api.posts(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải lịch sử tin nhắn")
        }
        val currentUserId = secureSessionManager.getUserId()
        return response.body()?.data?.content.orEmpty().map { it.toChatMessage(currentUserId) }
    }

    fun connect(groupId: Long, onEvent: (ChatRepositoryEvent) -> Unit) {
        webSocketClient.connect(groupId) { event ->
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

    fun send(groupId: Long, content: String, onError: (Throwable) -> Unit): Boolean {
        return webSocketClient.send(groupId, gson.toJson(mapOf("content" to content)), onError)
    }

    fun disconnect() = webSocketClient.disconnect()

    suspend fun leaveGroup(groupId: Long) {
        val response = api.leave(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể rời nhóm")
        }
    }

    suspend fun kickMember(groupId: Long, userId: Long) {
        val response = api.kickMember(groupId, userId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể mời thành viên rời nhóm")
        }
    }

    suspend fun reportPost(postId: Long, reason: String) {
        val response = api.reportPost(postId, com.example.carenest.feature.community.data.remote.ReportPostRequest(reason))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể báo cáo tin nhắn")
        }
    }

    private fun parseIncomingMessage(payload: String): ChatMessage {
        val currentUserId = secureSessionManager.getUserId()
        return runCatching { gson.fromJson(payload, GroupPost::class.java).toChatMessage(currentUserId) }
            .getOrElse {
                ChatMessage(
                    id = "raw-${java.util.UUID.randomUUID()}",
                    text = payload,
                    isMe = false,
                    senderName = "Thành viên",
                    timestamp = System.currentTimeMillis(),
                )
            }
    }

    private fun GroupPost.toChatMessage(currentUserId: Long?): ChatMessage {
        val isMe = (authorId != null) && (authorId == currentUserId)
        return ChatMessage(
            id = id.toString(),
            text = content,
            isMe = isMe,
            senderName = authorName ?: "Thành viên",
            senderId = authorId,
            senderRole = authorRole,
            replyPreview = replyToPostId?.let { "Đang trả lời một tin nhắn" },
            timestamp = createdAt.toEpochMillisOrNow(),
        )
    }

    private fun String?.toEpochMillisOrNow(): Long {
        if (this.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(this).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis())
    }
}
