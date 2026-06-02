package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.chat.data.repository.ChatRepository
import com.example.carenest.feature.chat.data.repository.ChatRepositoryEvent
import com.example.carenest.feature.chat.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatUiState(
    val isLoading: Boolean = true,
    val memberCount: Long? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isConnected: Boolean = false,
    val error: String? = null,
    val slowCountdown: Int = 0,
    val isSending: Boolean = false,
)

class ChatViewModel(
    private val groupId: Long,
    private val repository: ChatRepository,
) : ViewModel() {
    private var reconnectJob: Job? = null
    private var countdownJob: Job? = null
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadGroupPreview()
        loadHistory()
        connect()
    }

    fun onInputChange(value: String) = _uiState.update { it.copy(inputText = value) }

    fun sendMessage() {
        val content = _uiState.value.inputText.trim()
        if (content.isBlank() || _uiState.value.slowCountdown > 0 || _uiState.value.isSending) return

        val socketReady = _uiState.value.isConnected
        _uiState.update { it.copy(inputText = "", isSending = true, error = null) }

        viewModelScope.launch {
            if (socketReady) {
                sendWithSocket(content)
            } else {
                sendWithRestFallback(content, "Đã lưu tin nhắn. Realtime đang kết nối lại.")
            }
        }
    }

    private suspend fun sendWithSocket(content: String) {
        val optimistic = ChatMessage(
            id = "local-${java.util.UUID.randomUUID()}",
            text = content,
            isMe = true,
            senderName = "Tôi",
            timestamp = System.currentTimeMillis(),
        )

        val sent = withContext(Dispatchers.IO) {
            repository.sendOverSocket(groupId, content) {
                _uiState.update { current ->
                    current.copy(
                        isSending = false,
                        error = "Không thể gửi realtime. Đang thử kết nối dự phòng.",
                    )
                }
            }
        }

        if (sent) {
            _uiState.update {
                it.copy(
                    isSending = false,
                    messages = listOf(optimistic) + it.messages,
                    error = null,
                )
            }
            startSlowMode()
        } else {
            sendWithRestFallback(content, "Đã lưu qua kết nối dự phòng.")
        }
    }

    private suspend fun sendWithRestFallback(content: String, successMessage: String) {
        runCatching {
            withContext(Dispatchers.IO) {
                repository.sendViaRest(groupId, content)
            }
        }.onSuccess { saved ->
            _uiState.update { current ->
                val updatedMessages = if (current.messages.any { it.id == saved.id }) {
                    current.messages
                } else {
                    listOf(saved) + current.messages
                }
                current.copy(
                    isSending = false,
                    messages = updatedMessages,
                    error = successMessage,
                )
            }
            startSlowMode()
            reconnect()
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isSending = false,
                    inputText = content,
                    error = error.localizedMessage ?: "Không thể gửi tin nhắn. Vui lòng thử lại.",
                )
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.loadHistory(groupId)
                }
            }.onSuccess { messages ->
                _uiState.update { it.copy(isLoading = false, messages = messages) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.localizedMessage ?: "Không thể tải lịch sử tin nhắn",
                    )
                }
            }
        }
    }

    private fun loadGroupPreview() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.loadGroupPreview(groupId)
                }
            }.onSuccess { preview ->
                _uiState.update { it.copy(memberCount = preview.memberCount) }
            }
        }
    }

    private fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.connect(groupId) { event ->
                when (event) {
                    ChatRepositoryEvent.Connected -> {
                        _uiState.update { it.copy(isConnected = true, error = null) }
                    }

                    is ChatRepositoryEvent.Disconnected -> {
                        _uiState.update { it.copy(isConnected = false, error = event.message) }
                        reconnect()
                    }

                    is ChatRepositoryEvent.MessageReceived -> {
                        _uiState.update { current ->
                            if (current.messages.any { it.id == event.message.id }) {
                                current
                            } else {
                                current.copy(messages = listOf(event.message) + current.messages)
                            }
                        }
                    }
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

    fun leaveGroup(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.leaveGroup(groupId)
                }
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onError(it.localizedMessage ?: "Không thể rời nhóm")
            }
        }
    }

    fun kickMember(userId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.kickMember(groupId, userId)
                }
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onError(it.localizedMessage ?: "Không thể mời thành viên rời nhóm")
            }
        }
    }

    fun reportPost(postId: Long, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.reportPost(postId, reason)
                }
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onError(it.localizedMessage ?: "Không thể báo cáo tin nhắn")
            }
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
    private val repository: ChatRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(groupId, repository) as T
        }
        throw IllegalArgumentException("Không tìm thấy ViewModel phù hợp")
    }
}
