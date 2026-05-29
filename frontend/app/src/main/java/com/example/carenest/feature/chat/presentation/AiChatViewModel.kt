package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.chat.data.remote.AiChatApi
import com.example.carenest.feature.chat.data.remote.AiChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

class AiChatViewModel(
    private val aiChatApi: AiChatApi
) : ViewModel() {
    private val _messages = MutableStateFlow<List<AiMessage>>(
        listOf(AiMessage("Xin chào! Tôi là trợ lý AI CareNest. Bạn cần hỏi gì về sức khỏe gia đình?", isUser = false))
    )
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // Thêm tin nhắn của User
        _messages.value = _messages.value + AiMessage(text, isUser = true)
        _isTyping.value = true
        
        viewModelScope.launch {
            try {
                val response = aiChatApi.chatWithAi(AiChatRequest(message = text))
                if (response.isSuccessful && response.body() != null) {
                    _messages.value = _messages.value + AiMessage(response.body()!!.reply, isUser = false)
                } else {
                    _messages.value = _messages.value + AiMessage("Xin lỗi, hệ thống AI đang gặp trục trặc.", isUser = false)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + AiMessage("Không thể kết nối đến máy chủ AI (Lỗi: ${e.localizedMessage}).", isUser = false)
            } finally {
                _isTyping.value = false
            }
        }
    }
}

class AiChatViewModelFactory(
    private val aiChatApi: AiChatApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiChatViewModel(aiChatApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
