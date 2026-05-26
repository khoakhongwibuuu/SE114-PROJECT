package com.example.carenest.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.theme.PrimaryBlue
import com.example.carenest.viewmodel.ChatMessage
import com.example.carenest.viewmodel.ChatViewModel
import com.example.carenest.viewmodel.ChatViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    groupId: Long,
    groupName: String,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: ChatViewModel = viewModel(
        key = "chat-$groupId",
        factory = ChatViewModelFactory(
            groupId = groupId,
            repository = application.communityRepository,
            dataStoreManager = application.dataStoreManager
        )
    )
    val state by viewModel.uiState.collectAsState()
    val canSend = state.inputText.isNotBlank() && state.slowCountdown == 0

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(groupName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(
                                text = if (state.isConnected) "Đã kết nối" else state.error ?: "Đang kết nối lại...",
                                fontSize = 12.sp,
                                color = if (state.isConnected) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                HorizontalDivider(color = Color(0xFFE2E8F0))
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = state.inputText,
                    onValueChange = viewModel::onInputChange,
                    placeholder = {
                        Text(
                            if (state.slowCountdown > 0) "Chờ ${state.slowCountdown}s để gửi tiếp..." else "Nhập tin nhắn...",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 46.dp, max = 110.dp)
                        .clip(RoundedCornerShape(23.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = viewModel::sendMessage,
                    enabled = canSend,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (canSend) PrimaryBlue else Color(0xFFCBD5E1))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gửi", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    color = PrimaryBlue,
                    modifier = Modifier.align(Alignment.Center)
                )
                state.messages.isEmpty() -> EmptyChat()
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.messages, key = { it.id }) { msg ->
                        MessageBubble(msg)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChat() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Forum, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
        Text("Chưa có tin nhắn nào", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.padding(top = 10.dp))
        Text("Hãy bắt đầu cuộc trò chuyện đầu tiên trong nhóm.", fontSize = 14.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = formatter.format(Date(msg.timestamp))
    val isDoctor = msg.senderRole == "DOCTOR"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!msg.isMe) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDoctor) Color(0xFFE0F2FE) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(msg.senderName.take(1), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            if (!msg.isMe) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)) {
                    Text(msg.senderName, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    if (isDoctor) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = "Bác sĩ", tint = Color(0xFF0EA5E9), modifier = Modifier.size(14.dp))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (msg.isMe) 18.dp else 4.dp,
                            bottomEnd = if (msg.isMe) 4.dp else 18.dp
                        )
                    )
                    .background(if (msg.isMe) PrimaryBlue else if (isDoctor) Color(0xFFECFEFF) else Color(0xFFF1F5F9))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                msg.replyPreview?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (msg.isMe) Color(0x33111111) else Color.White)
                            .padding(8.dp)
                    ) {
                        Text(it, fontSize = 12.sp, color = if (msg.isMe) Color.White else PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    text = msg.text,
                    fontSize = 15.sp,
                    color = if (msg.isMe) Color.White else Color(0xFF1E293B),
                    lineHeight = 20.sp
                )
            }
            Text(timeStr, fontSize = 10.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp))
        }
    }
}
