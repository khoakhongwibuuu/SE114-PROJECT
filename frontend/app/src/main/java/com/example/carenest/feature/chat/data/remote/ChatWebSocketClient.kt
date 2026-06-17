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
    private val webSocketUrl: String = com.example.carenest.AppConfig.WEBSOCKET_URL,
) {
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null
    private var subscribedGroupId: Long? = null
    private var connectionId: Long = 0L
    private var isManualDisconnect = false
    private var disconnectNotified = false

    fun connect(groupId: Long, onEvent: (ChatSocketEvent) -> Unit) {
        disconnect()
        connectionId += 1
        val currentConnectionId = connectionId

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
        stompClient = client
        subscribedGroupId = null
        isManualDisconnect = false
        disconnectNotified = false

        disposables.add(
            client.lifecycle().subscribe({ event ->
                if (currentConnectionId != connectionId) return@subscribe
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("GroupChat", "WebSocket connected for group=$groupId")
                        subscribeToTopic(client, groupId, currentConnectionId, onEvent)
                        onEvent(ChatSocketEvent.Connected)
                    }

                    LifecycleEvent.Type.ERROR -> {
                        Log.e("GroupChat", "WebSocket error", event.exception)
                        notifyDisconnectOnce(
                            currentConnectionId = currentConnectionId,
                            message = "Mất kết nối phòng chat. Đang thử kết nối lại...",
                            onEvent = onEvent,
                        )
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        Log.w("GroupChat", "WebSocket closed for group=$groupId")
                        if (!isManualDisconnect) {
                            notifyDisconnectOnce(
                                currentConnectionId = currentConnectionId,
                                message = "Đang kết nối lại...",
                                onEvent = onEvent,
                            )
                        }
                    }

                    else -> Unit
                }
            }, { error ->
                Log.e("GroupChat", "Lifecycle subscription error", error)
                notifyDisconnectOnce(
                    currentConnectionId = currentConnectionId,
                    message = "Mất kết nối phòng chat. Đang thử kết nối lại...",
                    onEvent = onEvent,
                )
            }),
        )

        client.connect(headers())
    }

    fun send(groupId: Long, payload: String, onError: (Throwable) -> Unit): Boolean {
        val client = stompClient ?: return false
        disposables.add(
            client.send("/app/group/$groupId", payload).subscribe({}, onError),
        )
        return true
    }

    fun disconnect() {
        isManualDisconnect = true
        disposables.clear()
        subscribedGroupId = null
        stompClient?.disconnect()
        stompClient = null
    }

    private fun subscribeToTopic(
        client: StompClient,
        groupId: Long,
        currentConnectionId: Long,
        onEvent: (ChatSocketEvent) -> Unit,
    ) {
        if (subscribedGroupId == groupId) return
        subscribedGroupId = groupId
        disposables.add(
            client.topic("/topic/group/$groupId").subscribe({ message ->
                onEvent(ChatSocketEvent.MessageReceived(message.payload))
            }, { error ->
                Log.e("GroupChat", "Topic subscription error", error)
                notifyDisconnectOnce(
                    currentConnectionId = currentConnectionId,
                    message = "Không thể duy trì kết nối phòng chat. Đang thử kết nối lại...",
                    onEvent = onEvent,
                )
            }),
        )
    }

    private fun notifyDisconnectOnce(
        currentConnectionId: Long,
        message: String,
        onEvent: (ChatSocketEvent) -> Unit,
    ) {
        if (currentConnectionId != connectionId || isManualDisconnect || disconnectNotified) return
        disconnectNotified = true
        onEvent(ChatSocketEvent.Disconnected(message))
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
