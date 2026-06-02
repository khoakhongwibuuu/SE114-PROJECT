package com.example.carenest.feature.chat.data.remote

import android.util.Log
import com.example.carenest.core.data.storage.SecureSessionManager
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

sealed interface ChatSocketEvent {
    data object Connected : ChatSocketEvent
    data class Disconnected(val message: String) : ChatSocketEvent
    data class MessageReceived(val payload: String) : ChatSocketEvent
}

class ChatWebSocketClient(
    private val secureSessionManager: SecureSessionManager,
    private val webSocketUrl: String = com.example.carenest.AppConfig.WEBSOCKET_URL
) {
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null
    private var subscribedGroupId: Long? = null

    fun connect(groupId: Long, onEvent: (ChatSocketEvent) -> Unit) {
        disconnect()
        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
        stompClient = client
        subscribedGroupId = null

        disposables.add(
            client.lifecycle().subscribe({ event ->
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("GroupChat", "WebSocket connected for group=$groupId")
                        subscribeToTopic(client, groupId, onEvent)
                        onEvent(ChatSocketEvent.Connected)
                    }

                    LifecycleEvent.Type.ERROR -> {
                        Log.e("GroupChat", "WebSocket error", event.exception)
                        onEvent(
                            ChatSocketEvent.Disconnected(
                                event.exception?.localizedMessage ?: "Mất kết nối phòng chat"
                            )
                        )
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        Log.w("GroupChat", "WebSocket closed for group=$groupId")
                        onEvent(ChatSocketEvent.Disconnected("Đang kết nối lại..."))
                    }

                    else -> Unit
                }
            }, { error ->
                Log.e("GroupChat", "Lifecycle subscription error", error)
                onEvent(ChatSocketEvent.Disconnected(error.localizedMessage ?: "Mất kết nối phòng chat"))
            })
        )

        client.connect(headers())
    }

    fun send(groupId: Long, payload: String, onError: (Throwable) -> Unit): Boolean {
        val client = stompClient ?: return false
        disposables.add(
            client.send("/app/group/$groupId", payload).subscribe({}, onError)
        )
        return true
    }

    fun disconnect() {
        disposables.clear()
        subscribedGroupId = null
        stompClient?.disconnect()
        stompClient = null
    }

    private fun subscribeToTopic(
        client: StompClient,
        groupId: Long,
        onEvent: (ChatSocketEvent) -> Unit,
    ) {
        if (subscribedGroupId == groupId) return
        subscribedGroupId = groupId
        disposables.add(
            client.topic("/topic/group/$groupId").subscribe({ message ->
                onEvent(ChatSocketEvent.MessageReceived(message.payload))
            }, { error ->
                Log.e("GroupChat", "Topic subscription error", error)
                onEvent(ChatSocketEvent.Disconnected(error.localizedMessage ?: "Không thể kết nối phòng chat"))
            })
        )
    }

    private fun headers(): List<StompHeader> {
        val token = secureSessionManager.getAccessToken()
        return if (!token.isNullOrBlank()) {
            listOf(StompHeader("Authorization", "Bearer $token"))
        } else {
            emptyList()
        }
    }
}
