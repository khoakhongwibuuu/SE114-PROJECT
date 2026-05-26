package com.example.carenest.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.data.CommunityRepository
import com.example.carenest.data.DataStoreManager
import com.example.carenest.model.GroupPost
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.time.Instant

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val senderName: String,
    val senderRole: String? = null,
    val replyPreview: String? = null,
    val timestamp: Long
)

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
    private val repository: CommunityRepository,
    private val dataStoreManager: DataStoreManager,
    private val webSocketUrl: String = "ws://10.0.2.2:8080/ws"
) : ViewModel() {
    private val gson = Gson()
    private val disposables = CompositeDisposable()
    private var stompClient: StompClient? = null
    private var reconnectJob: Job? = null
    private var countdownJob: Job? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        connect()
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

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
        _uiState.update {
            it.copy(
                inputText = "",
                messages = listOf(optimistic) + it.messages,
                error = null
            )
        }
        startSlowMode()

        val payload = gson.toJson(mapOf("content" to content))
        val client = stompClient
        if (client == null || !_uiState.value.isConnected) {
            _uiState.update { it.copy(error = "Đang mất kết nối. Tin nhắn sẽ được gửi lại khi kết nối ổn định.") }
            reconnect()
            return
        }

        val disposable = client.send("/app/group/$groupId", payload).subscribe(
            {},
            { error ->
                val message = error.localizedMessage.orEmpty()
                _uiState.update {
                    it.copy(error = if (message.contains("slow", ignoreCase = true)) "Bạn đang gửi quá nhanh. Vui lòng chờ vài giây." else "Không thể gửi tin nhắn. Vui lòng thử lại.")
                }
            }
        )
        disposables.add(disposable)
    }

    fun reconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch {
            delay(2500)
            connect()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val posts = repository.posts(groupId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        messages = posts.map { post -> post.toChatMessage(isMe = false) }
                    )
                }
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
        runCatching {
            stompClient?.disconnect()
            disposables.clear()

            val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
            stompClient = client
            val token = dataStoreManager.getAccessToken()
            val headers = if (!token.isNullOrBlank()) {
                listOf(StompHeader("Authorization", "Bearer $token"))
            } else {
                emptyList()
            }

            disposables.add(
                client.lifecycle().subscribe { event ->
                    when (event.type) {
                        LifecycleEvent.Type.OPENED -> _uiState.update { it.copy(isConnected = true, error = null) }
                        LifecycleEvent.Type.ERROR -> {
                            Log.e("GroupChat", "Lỗi WebSocket", event.exception)
                            _uiState.update {
                                it.copy(
                                    isConnected = false,
                                    error = event.exception?.localizedMessage ?: "Mất kết nối phòng chat"
                                )
                            }
                            reconnect()
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            _uiState.update { it.copy(isConnected = false, error = "Đang kết nối lại...") }
                            reconnect()
                        }
                        else -> Unit
                    }
                }
            )

            disposables.add(
                client.topic("/topic/group/$groupId").subscribe { message ->
                    val incoming = parseIncomingMessage(message.payload)
                    _uiState.update { current ->
                        if (current.messages.any { it.id == incoming.id }) current
                        else current.copy(messages = listOf(incoming) + current.messages)
                    }
                }
            )

            client.connect(headers)
        }.onFailure { error ->
            Log.e("GroupChat", "Không thể kết nối WebSocket", error)
            _uiState.update {
                it.copy(
                    isConnected = false,
                    error = error.localizedMessage ?: "Không thể kết nối phòng chat"
                )
            }
            reconnect()
        }
    }

    private fun parseIncomingMessage(payload: String): ChatMessage {
        return runCatching {
            gson.fromJson(payload, GroupPost::class.java).toChatMessage(isMe = false)
        }.getOrElse {
            ChatMessage(
                id = "raw-${System.currentTimeMillis()}",
                text = payload,
                isMe = false,
                senderName = "Thành viên",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun GroupPost.toChatMessage(isMe: Boolean): ChatMessage {
        return ChatMessage(
            id = id.toString(),
            text = content,
            isMe = isMe,
            senderName = authorName ?: "Thành viên",
            senderRole = authorRole,
            replyPreview = replyToPostId?.let { "Đang trả lời một tin nhắn" },
            timestamp = createdAt.toEpochMillisOrNow()
        )
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

    private fun String?.toEpochMillisOrNow(): Long {
        if (this.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(this).toEpochMilli() }.getOrDefault(System.currentTimeMillis())
    }

    override fun onCleared() {
        countdownJob?.cancel()
        reconnectJob?.cancel()
        disposables.clear()
        stompClient?.disconnect()
        super.onCleared()
    }
}

class ChatViewModelFactory(
    private val groupId: Long,
    private val repository: CommunityRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(groupId, repository, dataStoreManager) as T
        }
        throw IllegalArgumentException("Không tìm thấy ViewModel phù hợp")
    }
}
