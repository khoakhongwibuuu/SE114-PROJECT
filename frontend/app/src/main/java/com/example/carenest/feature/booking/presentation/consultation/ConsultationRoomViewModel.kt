package com.example.carenest.feature.booking.presentation.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.feature.booking.data.remote.ConsultationSocketEvent
import com.example.carenest.feature.booking.domain.model.ConsultationMessage
import com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse
import com.example.carenest.feature.booking.domain.model.SendConsultationMessageRequest
import com.example.carenest.feature.booking.domain.port.BookingDataSource
import com.example.carenest.feature.booking.domain.port.ConsultationSocketGateway
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConsultationRoomState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val thread: ConsultationThreadResponse? = null,
    val messages: List<ConsultationMessage> = emptyList(),
    val isConnected: Boolean = false,
    val isActionLoading: Boolean = false,
    val actionSuccess: String? = null,
)

class ConsultationRoomViewModel(
    private val repository: BookingDataSource,
    private val webSocketClient: ConsultationSocketGateway,
    private val gson: Gson = Gson(),
) : ViewModel() {

    private val _state = MutableStateFlow(ConsultationRoomState())
    val state: StateFlow<ConsultationRoomState> = _state.asStateFlow()

    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0
    private var activeThreadId: Long? = null

    fun loadRoom(bookingId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.provisionConsultationThread(bookingId)
            result.onSuccess { thread ->
                reconnectAttempt = 0
                activeThreadId = thread.id
                _state.update { it.copy(thread = thread) }
                loadMessages(thread.id)
                connectWebSocket(thread.id)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.userMessage("Không thể vào phòng tư vấn")) }
            }
        }
    }

    private fun loadMessages(threadId: Long) {
        viewModelScope.launch {
            val result = repository.getConsultationMessages(threadId)
            result.onSuccess { msgs ->
                _state.update { it.copy(isLoading = false, messages = msgs) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.userMessage("Không thể tải tin nhắn tư vấn")) }
            }
        }
    }

    private fun connectWebSocket(threadId: Long) {
        webSocketClient.connect(threadId) { event ->
            when (event) {
                ConsultationSocketEvent.Connected -> {
                    reconnectAttempt = 0
                    reconnectJob?.cancel()
                    _state.update { it.copy(isConnected = true, error = null) }
                }

                is ConsultationSocketEvent.Disconnected -> {
                    val isReconnecting = event.message.contains("Đang kết nối lại", ignoreCase = true)
                    _state.update {
                        it.copy(
                            isConnected = false,
                            error = if (isReconnecting) null else event.message,
                        )
                    }
                    reconnect(threadId)
                }

                is ConsultationSocketEvent.MessageReceived -> {
                    try {
                        val newMessage = gson.fromJson(event.payload, ConsultationMessage::class.java)
                        if (newMessage.id <= 0L) {
                            _state.update {
                                it.copy(error = newMessage.content.removePrefix("ERROR:").trim())
                            }
                            return@connect
                        }
                        _state.update {
                            val newList = it.messages.toMutableList()
                            if (newList.none { msg -> msg.id == newMessage.id }) {
                                newList.add(newMessage)
                            }
                            it.copy(messages = newList)
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun reconnect(threadId: Long) {
        if (reconnectJob?.isActive == true) return
        if (reconnectAttempt >= 5) {
            _state.update {
                it.copy(
                    isConnected = false,
                    error = "Không thể kết nối lại phòng tư vấn sau nhiều lần thử. Vui lòng kiểm tra mạng.",
                )
            }
            return
        }
        reconnectAttempt++
        reconnectJob = viewModelScope.launch {
            delay(2000L * reconnectAttempt)
            if (activeThreadId == threadId) {
                connectWebSocket(threadId)
            }
        }
    }

    fun sendMessage(content: String): Boolean {
        val threadId = _state.value.thread?.id ?: return false
        val normalized = content.trim()
        if (normalized.isBlank()) {
            _state.update { it.copy(error = "Nội dung tin nhắn không được để trống") }
            return false
        }
        if (!_state.value.isConnected) {
            _state.update { it.copy(error = "Phòng tư vấn đang mất kết nối, vui lòng thử lại sau") }
            return false
        }
        val request = SendConsultationMessageRequest(content = normalized)
        val payload = gson.toJson(request)
        val queued = webSocketClient.send(threadId, payload) { error ->
            _state.update { it.copy(error = error.userMessage("Không thể gửi tin nhắn")) }
        }
        if (!queued) {
            _state.update { it.copy(error = "Phòng tư vấn chưa sẵn sàng để gửi tin nhắn") }
        }
        return queued
    }

    fun completeConsultation(bookingId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true, error = null) }
            val result = repository.completeConsultation(bookingId)
            result.onSuccess { updated ->
                _state.update {
                    it.copy(
                        thread = it.thread?.copy(status = updated.status),
                        isActionLoading = false,
                        actionSuccess = "Phiên tư vấn đã kết thúc.",
                    )
                }
                onDone()
            }.onFailure { e ->
                _state.update { it.copy(isActionLoading = false, error = e.userMessage("Không thể kết thúc phiên tư vấn")) }
            }
        }
    }

    fun restrictMessaging(bookingId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true, error = null) }
            val result = repository.restrictMessaging(bookingId)
            result.onSuccess { updated ->
                _state.update {
                    it.copy(
                        thread = it.thread?.copy(status = updated.status),
                        isActionLoading = false,
                        actionSuccess = "Đã hạn chế nhắn tin trong phiên này.",
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isActionLoading = false, error = e.userMessage("Không thể hạn chế nhắn tin")) }
            }
        }
    }

    fun unrestrictMessaging(bookingId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true, error = null) }
            val result = repository.unrestrictMessaging(bookingId)
            result.onSuccess { updated ->
                _state.update {
                    it.copy(
                        thread = it.thread?.copy(status = updated.status),
                        isActionLoading = false,
                        actionSuccess = "Đã hủy hạn chế nhắn tin thành công.",
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(isActionLoading = false, error = e.userMessage("Không thể hủy hạn chế nhắn tin")) }
            }
        }
    }

    fun clearActionSuccess() {
        _state.update { it.copy(actionSuccess = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        reconnectJob?.cancel()
        activeThreadId = null
        webSocketClient.disconnect()
        super.onCleared()
    }
}

class ConsultationRoomViewModelFactory(
    private val repository: BookingDataSource,
    private val webSocketClient: ConsultationSocketGateway,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConsultationRoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConsultationRoomViewModel(repository, webSocketClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
