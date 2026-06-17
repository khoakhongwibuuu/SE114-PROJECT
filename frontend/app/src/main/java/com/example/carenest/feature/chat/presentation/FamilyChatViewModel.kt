package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.chat.data.repository.ChatRepositoryEvent
import com.example.carenest.feature.chat.data.repository.FamilyChatRepository
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

data class FamilyChatUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isConnected: Boolean = false,
    val connectionHint: String? = null,
    val error: String? = null,
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val activeFamilyId: Long? = null,
    val isSending: Boolean = false,
)

class FamilyChatViewModel(
    private val repository: FamilyChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyChatUiState())
    val uiState: StateFlow<FamilyChatUiState> = _uiState.asStateFlow()

    private var boundFamilyId: Long? = null
    private var reconnectJob: Job? = null
    private var reconnectAttempt = 0
    private var loadedFamilyId: Long? = null

    companion object {
        private const val MAX_MESSAGE_LENGTH = 2000
    }

    fun bindFamily(familyId: Long) {
        if (boundFamilyId == familyId && loadedFamilyId == familyId) {
            reconnectAttempt = 0
            if (!_uiState.value.isConnected) connect(familyId)
            return
        }

        disconnect()
        reconnectAttempt = 0
        boundFamilyId = familyId
        _uiState.value = FamilyChatUiState(
            isLoading = true,
            activeFamilyId = familyId,
        )
        loadHistory(familyId, page = 0)
        connect(familyId)
    }

    fun unbind() {
        disconnect()
        reconnectJob?.cancel()
        reconnectJob = null

        _uiState.update {
            it.copy(
                isConnected = false,
                connectionHint = null,
                isSending = false,
            )
        }
    }

    fun onInputChange(value: String) {
        val normalized = value.take(MAX_MESSAGE_LENGTH)
        _uiState.update {
            it.copy(
                inputText = normalized,
                error = if (value.length > MAX_MESSAGE_LENGTH) {
                    "Tin nhắn không được vượt quá $MAX_MESSAGE_LENGTH ký tự"
                } else {
                    null
                }
            )
        }
    }

    fun sendMessage() {
        val familyId = boundFamilyId ?: return
        val content = _uiState.value.inputText.trim()
        if (content.isBlank() || _uiState.value.isSending) return

        if (!_uiState.value.isConnected) {
            _uiState.update {
                it.copy(error = "\u0110ang m\u1ea5t k\u1ebft n\u1ed1i ph\u00f2ng chat gia \u0111\u00ecnh. Vui l\u00f2ng th\u1eed l\u1ea1i sau.")
            }
            return
        }

        _uiState.update { it.copy(inputText = "", isSending = true, error = null) }
        val optimistic = ChatMessage(
            id = "local-${System.currentTimeMillis()}",
            text = content,
            isMe = true,
            senderName = "T\u00f4i",
            timestamp = System.currentTimeMillis(),
        )
        _uiState.update { it.copy(messages = listOf(optimistic) + it.messages) }

        val sent = repository.send(familyId, content) { throwable ->
            _uiState.update { current ->
                current.copy(
                    isSending = false,
                    messages = current.messages.filterNot { it.id == optimistic.id },
                    inputText = content,
                    error = throwable.localizedMessage
                        ?: "Kh\u00f4ng th\u1ec3 g\u1eedi tin nh\u1eafn gia \u0111\u00ecnh. Vui l\u00f2ng th\u1eed l\u1ea1i.",
                )
            }
        }

        if (sent) {
            _uiState.update { it.copy(isSending = false, error = null) }
        } else {
            _uiState.update { current ->
                current.copy(
                    isSending = false,
                    messages = current.messages.filterNot { it.id == optimistic.id },
                    inputText = content,
                    error = "Kh\u00f4ng th\u1ec3 g\u1eedi tin nh\u1eafn gia \u0111\u00ecnh. Vui l\u00f2ng th\u1eed l\u1ea1i.",
                )
            }
        }
    }

    fun loadMore() {
        val familyId = boundFamilyId ?: return
        val state = _uiState.value
        if (!state.hasMore || state.isLoading || state.isLoadingMore) return
        loadHistory(familyId, page = state.currentPage + 1)
    }

    private fun loadHistory(familyId: Long, page: Int) {
        viewModelScope.launch {
            if (page == 0) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
            }

            runCatching {
                withContext(Dispatchers.IO) { repository.loadHistory(familyId, page = page) }
            }.onSuccess { response ->
                val mapped = repository.mapToChatMessages(response)
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        messages = if (page == 0) mapped else current.messages + mapped,
                        hasMore = !response.last,
                        currentPage = response.number,
                        error = null,
                    )
                }
                if (page == 0) {
                    loadedFamilyId = familyId
                }
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = throwable.localizedMessage
                            ?: "Kh\u00f4ng th\u1ec3 t\u1ea3i l\u1ecbch s\u1eed tr\u00f2 chuy\u1ec7n gia \u0111\u00ecnh.",
                    )
                }
            }
        }
    }

    private fun connect(familyId: Long) {
        repository.connect(familyId) { event ->
            when (event) {
                ChatRepositoryEvent.Connected -> {
                    reconnectAttempt = 0
                    reconnectJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isConnected = true,
                            connectionHint = "\u0110ang tr\u00f2 chuy\u1ec7n c\u00f9ng gia \u0111\u00ecnh",
                            error = null,
                        )
                    }
                }

                is ChatRepositoryEvent.Disconnected -> {
                    _uiState.update {
                        it.copy(
                            isConnected = false,
                            connectionHint = event.message,
                        )
                    }
                    reconnect(familyId)
                }

                is ChatRepositoryEvent.MessageReceived -> {
                    _uiState.update { current ->
                        val withoutOptimistic = current.messages.filterNot {
                            it.isMe && it.text == event.message.text && it.id.startsWith("local-")
                        }
                        if (withoutOptimistic.any { it.id == event.message.id }) {
                            current
                        } else {
                            current.copy(messages = listOf(event.message) + withoutOptimistic)
                        }
                    }
                }
            }
        }
    }

    private fun reconnect(familyId: Long) {
        if (reconnectJob?.isActive == true) return
        if (reconnectAttempt >= 5) {
            _uiState.update {
                it.copy(
                    isConnected = false,
                    connectionHint = null,
                    error = "Không thể kết nối lại gia đình sau nhiều lần thử."
                )
            }
            return
        }
        reconnectAttempt++
        reconnectJob = viewModelScope.launch {
            delay(2000L * reconnectAttempt)
            if (boundFamilyId == familyId) {
                connect(familyId)
            }
        }
    }

    private fun disconnect() {
        repository.disconnect()
        reconnectJob?.cancel()
    }

    override fun onCleared() {
        disconnect()
        super.onCleared()
    }
}

class FamilyChatViewModelFactory(
    private val repository: FamilyChatRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FamilyChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
