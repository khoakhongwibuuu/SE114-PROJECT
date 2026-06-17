package com.example.carenest.feature.chat.data.remote

import android.util.Log
import com.example.carenest.core.data.storage.SecureSessionManager
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

class FamilyChatWebSocketClient(
    private val secureSessionManager: SecureSessionManager,
    private val webSocketUrl: String = com.example.carenest.AppConfig.WEBSOCKET_URL,
) {
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null
    private var subscribedFamilyId: Long? = null
    private var connectionId: Long = 0L
    private var isManualDisconnect = false
    private var disconnectNotified = false

    fun connect(familyId: Long, onEvent: (ChatSocketEvent) -> Unit) {
        disconnect()
        connectionId += 1
        val currentConnectionId = connectionId

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
        stompClient = client
        subscribedFamilyId = null
        isManualDisconnect = false
        disconnectNotified = false

        disposables.add(
            client.lifecycle().subscribe({ event ->
                if (currentConnectionId != connectionId) return@subscribe
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("FamilyChatWS", "WebSocket connected for family=$familyId")
                        subscribeToTopic(client, familyId, currentConnectionId, onEvent)
                        onEvent(ChatSocketEvent.Connected)
                    }

                    LifecycleEvent.Type.ERROR -> {
                        Log.e("FamilyChatWS", "WebSocket error", event.exception)
                        notifyDisconnectOnce(
                            currentConnectionId = currentConnectionId,
                            message = "Mất kết nối phòng chat gia đình. Đang thử kết nối lại...",
                            onEvent = onEvent,
                        )
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        Log.w("FamilyChatWS", "WebSocket closed for family=$familyId")
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
                Log.e("FamilyChatWS", "Lifecycle subscription error", error)
                notifyDisconnectOnce(
                    currentConnectionId = currentConnectionId,
                    message = "Mất kết nối phòng chat gia đình. Đang thử kết nối lại...",
                    onEvent = onEvent,
                )
            }),
        )

        client.connect(headers())
    }

    fun send(payload: String, onError: (Throwable) -> Unit): Boolean {
        val client = stompClient ?: return false
        val publishDestination = "/app/chat.sendMessage"
        Log.d("FamilyChatWS", "Publishing message to: $publishDestination")
        disposables.add(
            client.send(publishDestination, payload).subscribe({
                Log.d("FamilyChatWS", "Message sent successfully to STOMP server.")
            }, {
                Log.e("FamilyChatWS", "Failed to send STOMP message", it)
                onError(it)
            }),
        )
        return true
    }

    fun disconnect() {
        Log.d("FamilyChatWS", "Disconnecting STOMP WebSocket client.")
        isManualDisconnect = true
        disposables.clear()
        subscribedFamilyId = null
        stompClient?.disconnect()
        stompClient = null
    }

    private fun subscribeToTopic(
        client: StompClient,
        familyId: Long,
        currentConnectionId: Long,
        onEvent: (ChatSocketEvent) -> Unit,
    ) {
        if (subscribedFamilyId == familyId) return
        subscribedFamilyId = familyId
        val topicDestination = "/topic/family/$familyId"
        Log.d("FamilyChatWS", "Subscribing to topic: $topicDestination")
        disposables.add(
            client.topic(topicDestination).subscribe({ message ->
                onEvent(ChatSocketEvent.MessageReceived(message.payload))
            }, { error ->
                Log.e("FamilyChatWS", "Topic subscription error", error)
                notifyDisconnectOnce(
                    currentConnectionId = currentConnectionId,
                    message = "Không thể duy trì kết nối phòng chat gia đình. Đang thử kết nối lại...",
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
