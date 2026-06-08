package com.example.carenest.feature.chat.data.repository

import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.chat.data.remote.ChatSocketEvent
import com.example.carenest.feature.chat.data.remote.ChatWebSocketClient
import com.example.carenest.feature.chat.domain.model.ChatGroupPreview
import com.example.carenest.feature.chat.domain.model.ChatMessage
import com.example.carenest.feature.community.data.remote.CommunityApi
import com.example.carenest.feature.community.data.remote.ReportPostRequest
import com.example.carenest.feature.community.domain.model.CreateGroupPostRequest
import com.example.carenest.feature.community.domain.model.GroupPost
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

    suspend fun loadGroupPreview(groupId: Long): ChatGroupPreview {
        val response = api.preview(groupId)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể tải thông tin nhóm")
        }
        return response.body()?.data
            ?: throw IllegalStateException("Không nhận được thông tin nhóm")
    }

    suspend fun sendViaRest(groupId: Long, content: String): ChatMessage {
        val response = api.sendPost(groupId, CreateGroupPostRequest(content = content))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể gửi tin nhắn")
        }

        val post = response.body()?.data
            ?: throw IllegalStateException("Không nhận được tin nhắn mới")
        return post.toChatMessage(secureSessionManager.getUserId())
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
        val payload = gson.toJson(mapOf("content" to content))
        return webSocketClient.send(groupId, payload, onError)
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
        val response = api.reportPost(postId, ReportPostRequest(reason))
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Không thể báo cáo tin nhắn")
        }
    }

    private fun parseIncomingMessage(payload: String): ChatMessage {
        val currentUserId = secureSessionManager.getUserId()
        return runCatching { gson.fromJson(payload, GroupPost::class.java).toChatMessage(currentUserId) }
            .getOrElse {
                ChatMessage(
                    id = "raw-${UUID.randomUUID()}",
                    text = payload,
                    isMe = false,
                    senderName = "Thành viên",
                    timestamp = System.currentTimeMillis(),
                )
            }
    }

    private fun GroupPost.toChatMessage(currentUserId: Long?): ChatMessage {
        return ChatMessage(
            id = id.toString(),
            text = content,
            isMe = authorId != null && authorId == currentUserId,
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
