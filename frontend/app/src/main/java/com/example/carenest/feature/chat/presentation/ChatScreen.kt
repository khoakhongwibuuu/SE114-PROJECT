package com.example.carenest.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.chat.presentation.components.MessageBubble

// Legacy RN chat colors
private val ChatBlue = Color(0xFF1A73E8)
private val ChatBackIcon = Color(0xFF0369A1)

@Composable
fun ChatScreen(
    groupId: Long,
    groupName: String,
    onBack: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: ChatViewModel = viewModel(
        key = "chat-$groupId",
        factory = ChatViewModelFactory(groupId = groupId, repository = application.chatRepository),
    )
    val state by viewModel.uiState.collectAsState()
    val canSend = state.inputText.isNotBlank() && state.slowCountdown == 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF4F8))
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = ChatBackIcon,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = groupName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                )
                Text(
                    text = if (state.isConnected) "Phòng trò chuyện cộng đồng" else (state.error ?: "Đang kết nối lại..."),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isConnected) Color(0xFF94A3B8) else Color(0xFFDC2626),
                )
            }

            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn nhóm", tint = Color(0xFF0F172A))
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFBEB))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nội dung trong phòng chat chỉ mang tính tham khảo, không thay thế tư vấn, chẩn đoán hoặc điều trị y khoa trực tiếp.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 17.sp,
                color = Color(0xFF92400E),
            )
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }

            state.messages.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(42.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có tin nhắn",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hãy bắt đầu cuộc trò chuyện đầu tiên trong nhóm.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageBubble(msg = message)
                    }
                }
            }
        }

        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .windowInsetsPadding(WindowInsets.ime),
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                TextField(
                    value = state.inputText,
                    onValueChange = viewModel::onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 112.dp),
                    placeholder = {
                        Text(
                            text = if (state.slowCountdown > 0) {
                                "Chờ ${state.slowCountdown}s để gửi tiếp..."
                            } else {
                                "Nhập tin nhắn..."
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(22.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) ChatBlue else Color(0xFFCBD5E1)),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = viewModel::sendMessage, enabled = canSend) {
                        if (state.slowCountdown > 0) {
                            Text(
                                text = state.slowCountdown.toString(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
