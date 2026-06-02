package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.chat.data.repository.FamilyChatRepository
import com.example.carenest.feature.chat.data.repository.ChatRepositoryEvent
import com.example.carenest.feature.chat.domain.model.ChatMessage
import com.example.carenest.feature.family.data.repository.FamilyRepository
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
    val isLoading: Boolean = true,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isConnected: Boolean = false,
    val error: String? = null,
    val isConnecting: Boolean = true,
    val memberCount: Int? = null
)

class FamilyChatViewModel(
    private val familyId: Long,
    private val repository: FamilyChatRepository,
    private val familyRepository: FamilyRepository
) : ViewModel() {
    private var reconnectJob: Job? = null
    private val _uiState = MutableStateFlow(FamilyChatUiState())
    val uiState: StateFlow<FamilyChatUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var hasMore = true
    private var isLoadingMore = false

    init {
        loadHistory()
        loadFamilyDetails()
        connect()
    }

    fun onInputChange(value: String) = _uiState.update { it.copy(inputText = value) }

    fun sendMessage() {
        val content = _uiState.value.inputText.trim()
        if (content.isBlank()) return

        // Optimistic UI: display immediately
        val optimistic = ChatMessage(
            id = "local-${java.util.UUID.randomUUID()}",
            text = content,
            isMe = true,
            senderName = "Tôi",
            timestamp = System.currentTimeMillis()
        )
        _uiState.update {
            it.copy(
                inputText = "",
                messages = listOf(optimistic) + it.messages,
                error = null
            )
        }

        viewModelScope.launch {
            val sent = _uiState.value.isConnected && withContext(Dispatchers.IO) {
                repository.send(familyId, content) { error ->
                    _uiState.update { current ->
                        current.copy(error = "Không thể gửi tin nhắn: ${error.localizedMessage}")
                    }
                }
            }

            if (!sent) {
                _uiState.update {
                    it.copy(error = "Mất kết nối. Tin nhắn sẽ được gửi lại khi trực tuyến.")
                }
                reconnect()
            }
        }
    }

    fun loadEarlierMessages() {
        if (!hasMore || isLoadingMore) return
        isLoadingMore = true
        viewModelScope.launch {
            try {
                val pageResponse = withContext(Dispatchers.IO) {
                    repository.loadHistory(familyId, currentPage + 1)
                }
                val newMessages = repository.mapToChatMessages(pageResponse)
                _uiState.update { current ->
                    val existingIds = current.messages.map { it.id }.toSet()
                    val filteredNew = newMessages.filter { it.id !in existingIds }
                    current.copy(messages = current.messages + filteredNew)
                }
                currentPage = pageResponse.number
                hasMore = !pageResponse.last
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi tải tin nhắn cũ: ${e.localizedMessage}") }
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun loadFamilyDetails() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    familyRepository.getFamilyById(familyId)
                }
                result.onSuccess { detail ->
                    _uiState.update { it.copy(memberCount = detail.members.size) }
                }
            } catch (e: Exception) {
                // Silently handle metadata failure; chat can proceed
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val pageResponse = withContext(Dispatchers.IO) {
                    repository.loadHistory(familyId, 0)
                }
                val messages = repository.mapToChatMessages(pageResponse)
                currentPage = pageResponse.number
                hasMore = !pageResponse.last
                _uiState.update { it.copy(isLoading = false, messages = messages) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Không thể tải lịch sử tin nhắn"
                    )
                }
            }
        }
    }

    private fun connect() {
        _uiState.update { it.copy(isConnecting = true) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.connect(familyId) { event ->
                when (event) {
                    ChatRepositoryEvent.Connected -> {
                        _uiState.update { it.copy(isConnected = true, isConnecting = false, error = null) }
                    }
                    is ChatRepositoryEvent.Disconnected -> {
                        _uiState.update { it.copy(isConnected = false, isConnecting = false, error = event.message) }
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
            delay(3000)
            connect()
        }
    }

    override fun onCleared() {
        reconnectJob?.cancel()
        repository.disconnect()
        super.onCleared()
    }
}

class FamilyChatViewModelFactory(
    private val familyId: Long,
    private val repository: FamilyChatRepository,
    private val familyRepository: FamilyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FamilyChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FamilyChatViewModel(familyId, repository, familyRepository) as T
        }
        throw IllegalArgumentException("Không tìm thấy ViewModel phù hợp")
    }
}
