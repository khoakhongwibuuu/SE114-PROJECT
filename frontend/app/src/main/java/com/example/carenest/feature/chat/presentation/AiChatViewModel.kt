package com.example.carenest.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.network.errorMessage
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
    private val _messages = MutableStateFlow(
        listOf(
            AiMessage(
                text = "Xin chào! Tôi là CareNest AI, trợ lý sức khỏe gia đình.\n\n" +
                    "Tôi có thể giúp bạn tóm tắt thông tin sức khỏe, gợi ý câu hỏi cần hỏi bác sĩ và nhắc kiểm tra thông tin thuốc.\n\n" +
                    "Lưu ý: AI chỉ hỗ trợ tham khảo, không thay thế chẩn đoán hoặc tư vấn y tế trực tiếp.",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    private var conversationId: Long? = null

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isTyping.value) return

        _messages.value = _messages.value + AiMessage(trimmed, isUser = true)
        _isTyping.value = true

        viewModelScope.launch {
            try {
                val response = aiChatApi.chatWithAi(
                    AiChatRequest(message = trimmed, conversationId = conversationId)
                )
                if (response.isSuccessful) {
                    val envelope = response.body()
                    val reply = envelope?.data?.reply
                        ?: envelope?.message
                        ?: "Máy chủ AI không trả về nội dung."
                    conversationId = envelope?.data?.conversationId ?: conversationId
                    _messages.value = _messages.value + AiMessage(reply, isUser = false)
                } else {
                    _messages.value = _messages.value + AiMessage(
                        response.errorMessage("Hệ thống AI đang gặp sự cố."),
                        isUser = false
                    )
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + AiMessage(
                    "Không thể kết nối đến máy chủ AI (${e.localizedMessage ?: "lỗi không xác định"}).",
                    isUser = false
                )
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
