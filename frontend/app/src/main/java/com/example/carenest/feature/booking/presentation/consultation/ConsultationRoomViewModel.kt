package com.example.carenest.feature.booking.presentation.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.booking.data.remote.ConsultationSocketEvent
import com.example.carenest.feature.booking.data.remote.ConsultationWebSocketClient
import com.example.carenest.feature.booking.domain.model.ConsultationMessage
import com.example.carenest.feature.booking.domain.model.ConsultationThreadResponse
import com.example.carenest.feature.booking.domain.model.SendConsultationMessageRequest
import com.example.carenest.feature.booking.data.repository.BookingRepository
import com.google.gson.Gson
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
    val actionSuccess: String? = null
)

class ConsultationRoomViewModel(
    private val repository: BookingRepository,
    private val webSocketClient: ConsultationWebSocketClient,
    private val gson: Gson = Gson()
) : ViewModel() {

    private val _state = MutableStateFlow(ConsultationRoomState())
    val state: StateFlow<ConsultationRoomState> = _state.asStateFlow()

    fun loadRoom(bookingId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.provisionConsultationThread(bookingId)
            result.onSuccess { thread ->
                _state.update { it.copy(thread = thread) }
                loadMessages(thread.id)
                connectWebSocket(thread.id)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Lỗi khi vào phòng tư vấn") }
            }
        }
    }

    private fun loadMessages(threadId: Long) {
        viewModelScope.launch {
            val result = repository.getConsultationMessages(threadId)
            result.onSuccess { msgs ->
                _state.update { it.copy(isLoading = false, messages = msgs) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message ?: "Lỗi khi tải tin nhắn") }
            }
        }
    }

    private fun connectWebSocket(threadId: Long) {
        webSocketClient.connect(threadId) { event ->
            when (event) {
                is ConsultationSocketEvent.Connected -> {
                    _state.update { it.copy(isConnected = true, error = null) }
                }
                is ConsultationSocketEvent.Disconnected -> {
                    _state.update { it.copy(isConnected = false, error = event.message) }
                }
                is ConsultationSocketEvent.MessageReceived -> {
                    try {
                        val newMessage = gson.fromJson(event.payload, ConsultationMessage::class.java)
                        _state.update {
                            val newList = it.messages.toMutableList()
                            // Simple deduplication based on ID if needed, but append is fine
                            if (newList.none { msg -> msg.id == newMessage.id }) {
                                newList.add(newMessage)
                            }
                            it.copy(messages = newList)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun sendMessage(content: String) {
        val threadId = _state.value.thread?.id ?: return
        val request = SendConsultationMessageRequest(content = content)
        val payload = gson.toJson(request)
        webSocketClient.send(threadId, payload) { error ->
            _state.update { it.copy(error = error.message ?: "Không thể gửi tin nhắn") }
        }
    }

    fun completeConsultation(bookingId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            val result = repository.completeConsultation(bookingId)
            result.onSuccess { updated ->
                _state.update { it.copy(
                    thread = it.thread?.copy(status = updated.status),
                    actionSuccess = "Phiên tư vấn đã kết thúc."
                )}
                onDone()
            }.onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun restrictMessaging(bookingId: Long) {
        viewModelScope.launch {
            val result = repository.restrictMessaging(bookingId)
            result.onSuccess { updated ->
                _state.update { it.copy(
                    thread = it.thread?.copy(status = updated.status),
                    actionSuccess = "Đã hạn chế nhắn tin trong phiên này."
                )}
            }.onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun unrestrictMessaging(bookingId: Long) {
        viewModelScope.launch {
            val result = repository.unrestrictMessaging(bookingId)
            result.onSuccess { updated ->
                _state.update { it.copy(
                    thread = it.thread?.copy(status = updated.status),
                    actionSuccess = "Đã hủy hạn chế nhắn tin thành công."
                )}
            }.onFailure { e ->
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearActionSuccess() {
        _state.update { it.copy(actionSuccess = null) }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}

class ConsultationRoomViewModelFactory(
    private val repository: BookingRepository,
    private val webSocketClient: ConsultationWebSocketClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConsultationRoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConsultationRoomViewModel(repository, webSocketClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
