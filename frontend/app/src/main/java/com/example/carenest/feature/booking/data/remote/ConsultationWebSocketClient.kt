package com.example.carenest.feature.booking.data.remote

import android.util.Log
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.booking.domain.port.ConsultationSocketGateway
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
    private val webSocketUrl: String = com.example.carenest.AppConfig.WEBSOCKET_URL,
) : ConsultationSocketGateway {
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null
    private var subscribedThreadId: Long? = null
    private var connectionId: Long = 0L
    private var isManualDisconnect = false
    private var disconnectNotified = false

    override fun connect(threadId: Long, onEvent: (ConsultationSocketEvent) -> Unit) {
        disconnect()
        connectionId += 1
        val currentConnectionId = connectionId

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
        stompClient = client
        subscribedThreadId = null
        isManualDisconnect = false
        disconnectNotified = false

        disposables.add(
            client.lifecycle().subscribe({ event ->
                if (currentConnectionId != connectionId) return@subscribe
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("ConsultationChat", "WebSocket connected for thread=$threadId")
                        subscribeToTopic(client, threadId, currentConnectionId, onEvent)
                        onEvent(ConsultationSocketEvent.Connected)
                    }

                    LifecycleEvent.Type.ERROR -> {
                        Log.e("ConsultationChat", "WebSocket error", event.exception)
                        notifyDisconnectOnce(
                            currentConnectionId = currentConnectionId,
                            message = "Mất kết nối phòng tư vấn. Đang thử kết nối lại...",
                            onEvent = onEvent,
                        )
                    }

                    LifecycleEvent.Type.CLOSED -> {
                        Log.w("ConsultationChat", "WebSocket closed for thread=$threadId")
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
                Log.e("ConsultationChat", "Lifecycle subscription error", error)
                notifyDisconnectOnce(
                    currentConnectionId = currentConnectionId,
                    message = "Mất kết nối phòng tư vấn. Đang thử kết nối lại...",
                    onEvent = onEvent,
                )
            }),
        )

        client.connect(headers())
    }

    override fun send(threadId: Long, payload: String, onError: (Throwable) -> Unit): Boolean {
        val client = stompClient ?: return false
        disposables.add(
            client.send("/app/consultation/thread/$threadId", payload).subscribe({}, onError),
        )
        return true
    }

    override fun disconnect() {
        isManualDisconnect = true
        disposables.clear()
        subscribedThreadId = null
        stompClient?.disconnect()
        stompClient = null
    }

    private fun subscribeToTopic(
        client: StompClient,
        threadId: Long,
        currentConnectionId: Long,
        onEvent: (ConsultationSocketEvent) -> Unit,
    ) {
        if (subscribedThreadId == threadId) return
        subscribedThreadId = threadId
        disposables.add(
            client.topic("/topic/consultation/thread/$threadId").subscribe({ message ->
                onEvent(ConsultationSocketEvent.MessageReceived(message.payload))
            }, { error ->
                Log.e("ConsultationChat", "Topic subscription error", error)
                notifyDisconnectOnce(
                    currentConnectionId = currentConnectionId,
                    message = "Không thể duy trì kết nối phòng tư vấn. Đang thử kết nối lại...",
                    onEvent = onEvent,
                )
            }),
        )
    }

    private fun notifyDisconnectOnce(
        currentConnectionId: Long,
        message: String,
        onEvent: (ConsultationSocketEvent) -> Unit,
    ) {
        if (currentConnectionId != connectionId || isManualDisconnect || disconnectNotified) return
        disconnectNotified = true
        onEvent(ConsultationSocketEvent.Disconnected(message))
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
