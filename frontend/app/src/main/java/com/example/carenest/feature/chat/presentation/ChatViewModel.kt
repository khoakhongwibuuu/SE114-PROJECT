package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.chat.data.repository.ChatRepository
import com.example.carenest.feature.chat.data.repository.ChatRepositoryEvent
import com.example.carenest.feature.chat.domain.model.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = true,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isConnected: Boolean = false,
    val error: String? = null,
    val slowCountdown: Int = 0
)

class ChatViewModel(
    private val groupId: Long,
    private val repository: ChatRepository
) : ViewModel() {
    private var reconnectJob: Job? = null
    private var countdownJob: Job? = null
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        connect()
    }

    fun onInputChange(value: String) = _uiState.update { it.copy(inputText = value) }

    fun sendMessage() {
        val content = _uiState.value.inputText.trim()
        if (content.isBlank() || _uiState.value.slowCountdown > 0) return

        val optimistic = ChatMessage(
            id = "local-${System.currentTimeMillis()}",
            text = content,
            isMe = true,
            senderName = "Tôi",
            timestamp = System.currentTimeMillis()
        )
        _uiState.update { it.copy(inputText = "", messages = listOf(optimistic) + it.messages, error = null) }
        startSlowMode()

        val sent = _uiState.value.isConnected && repository.send(groupId, content) { error ->
            val message = error.localizedMessage.orEmpty()
            _uiState.update { current ->
                current.copy(
                    error = if (message.contains("slow", ignoreCase = true)) {
                        "Bạn đang gửi quá nhanh. Vui lòng chờ vài giây."
                    } else {
                        "Không thể gửi tin nhắn. Vui lòng thử lại."
                    }
                )
            }
        }

        if (!sent) {
            _uiState.update { it.copy(error = "Đang mất kết nối. Tin nhắn sẽ được gửi lại khi kết nối ổn định.") }
            reconnect()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                _uiState.update { it.copy(isLoading = false, messages = repository.loadHistory(groupId)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Không thể tải lịch sử tin nhắn") }
            }
        }
    }

    private fun connect() {
        repository.connect(groupId) { event ->
            when (event) {
                ChatRepositoryEvent.Connected -> _uiState.update { it.copy(isConnected = true, error = null) }
                is ChatRepositoryEvent.Disconnected -> {
                    _uiState.update { it.copy(isConnected = false, error = event.message) }
                    reconnect()
                }
                is ChatRepositoryEvent.MessageReceived -> _uiState.update { current ->
                    if (current.messages.any { it.id == event.message.id }) current
                    else current.copy(messages = listOf(event.message) + current.messages)
                }
            }
        }
    }

    private fun reconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch {
            delay(2500)
            connect()
        }
    }

    private fun startSlowMode() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (remaining in 5 downTo 1) {
                _uiState.update { it.copy(slowCountdown = remaining) }
                delay(1000)
            }
            _uiState.update { it.copy(slowCountdown = 0) }
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        reconnectJob?.cancel()
        repository.disconnect()
        super.onCleared()
    }
}

class ChatViewModelFactory(
    private val groupId: Long,
    private val repository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(groupId, repository) as T
        }
        throw IllegalArgumentException("Không tìm thấy ViewModel phù hợp")
    }
}
