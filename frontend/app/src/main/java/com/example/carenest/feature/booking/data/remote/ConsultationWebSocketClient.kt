package com.example.carenest.feature.booking.data.remote

import android.util.Log
import com.example.carenest.core.data.storage.SecureSessionManager
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

sealed interface ConsultationSocketEvent {
    data object Connected : ConsultationSocketEvent
    data class Disconnected(val message: String) : ConsultationSocketEvent
    data class MessageReceived(val payload: String) : ConsultationSocketEvent
}

class ConsultationWebSocketClient(
    private val secureSessionManager: SecureSessionManager,
    private val webSocketUrl: String = com.example.carenest.AppConfig.WEBSOCKET_URL
) {
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null
    private var subscribedThreadId: Long? = null

    fun connect(threadId: Long, onEvent: (ConsultationSocketEvent) -> Unit) {
        disconnect()
        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
        stompClient = client
        subscribedThreadId = null

        disposables.add(
            client.lifecycle().subscribe({ event ->
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("ConsultationChat", "WebSocket connected for thread=$threadId")
                        subscribeToTopic(client, threadId, onEvent)
                        onEvent(ConsultationSocketEvent.Connected)
                    }

                    LifecycleEvent.Type.ERROR -> {
                        Log.e("ConsultationChat", "WebSocket error", event.exception)
                        onEvent(
                            ConsultationSocketEvent.Disconnected(
                                event.exception?.localizedMessage ?: "Mất kết nối phòng tư vấn"
                            )
                        )
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        Log.w("ConsultationChat", "WebSocket closed for thread=$threadId")
                        onEvent(ConsultationSocketEvent.Disconnected("Đang kết nối lại..."))
                    }

                    else -> Unit
                }
            }, { error ->
                Log.e("ConsultationChat", "Lifecycle subscription error", error)
                onEvent(ConsultationSocketEvent.Disconnected(error.localizedMessage ?: "Mất kết nối phòng tư vấn"))
            })
        )

        client.connect(headers())
    }

    fun send(threadId: Long, payload: String, onError: (Throwable) -> Unit): Boolean {
        val client = stompClient ?: return false
        disposables.add(
            client.send("/app/consultation/thread/$threadId", payload).subscribe({}, onError)
        )
        return true
    }

    fun disconnect() {
        disposables.clear()
        subscribedThreadId = null
        stompClient?.disconnect()
        stompClient = null
    }

    private fun subscribeToTopic(
        client: StompClient,
        threadId: Long,
        onEvent: (ConsultationSocketEvent) -> Unit,
    ) {
        if (subscribedThreadId == threadId) return
        subscribedThreadId = threadId
        disposables.add(
            client.topic("/topic/consultation/thread/$threadId").subscribe({ message ->
                onEvent(ConsultationSocketEvent.MessageReceived(message.payload))
            }, { error ->
                Log.e("ConsultationChat", "Topic subscription error", error)
                onEvent(ConsultationSocketEvent.Disconnected(error.localizedMessage ?: "Không thể kết nối phòng tư vấn"))
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
