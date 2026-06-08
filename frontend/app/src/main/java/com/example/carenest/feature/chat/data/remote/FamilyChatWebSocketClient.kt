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

    fun connect(familyId: Long, onEvent: (ChatSocketEvent) -> Unit) {
        disconnect()
        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
        stompClient = client
        subscribedFamilyId = null

        disposables.add(
            client.lifecycle().subscribe({ event ->
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("FamilyChatWS", "WebSocket connected for family=$familyId")
                        subscribeToTopic(client, familyId, onEvent)
                        onEvent(ChatSocketEvent.Connected)
                    }

                    LifecycleEvent.Type.ERROR -> {
                        Log.e("FamilyChatWS", "WebSocket error", event.exception)
                        onEvent(
                            ChatSocketEvent.Disconnected(
                                event.exception?.localizedMessage
                                    ?: "Mất kết nối phòng chat gia đình",
                            ),
                        )
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        Log.w("FamilyChatWS", "WebSocket closed for family=$familyId")
                        onEvent(ChatSocketEvent.Disconnected("Đang kết nối lại..."))
                    }

                    else -> Unit
                }
            }, { error ->
                Log.e("FamilyChatWS", "Lifecycle subscription error", error)
                onEvent(
                    ChatSocketEvent.Disconnected(
                        error.localizedMessage ?: "Mất kết nối phòng chat gia đình",
                    ),
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
        disposables.clear()
        subscribedFamilyId = null
        stompClient?.disconnect()
        stompClient = null
    }

    private fun subscribeToTopic(
        client: StompClient,
        familyId: Long,
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
                onEvent(
                    ChatSocketEvent.Disconnected(
                        error.localizedMessage ?: "Không thể kết nối phòng chat gia đình",
                    ),
                )
            }),
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
