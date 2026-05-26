package com.example.carenest.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.theme.PrimaryBlue
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val senderName: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(familyId: String, familyName: String, onBack: () -> Unit) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    
    // Giả lập WS Client (NaikSoftware StompProtocolAndroid)
    val stompClient = remember {
        // Trong thực tế sẽ là: Stomp.over(Stomp.ConnectionProvider.JWS, "ws://10.0.2.2:8080/ws/chat")
        // Ở đây ta mock logic STOMP để đảm bảo UI không crash nếu chưa bật backend
        Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/chat")
    }

    LaunchedEffect(Unit) {
        try {
            stompClient.connect()
            stompClient.lifecycle().subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        isConnected = true
                        Log.d("STOMP", "Stomp connection opened")
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                        Log.e("STOMP", "Error", lifecycleEvent.exception)
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                        isConnected = false
                        Log.d("STOMP", "Stomp connection closed")
                    }
                    else -> {}
                }
            }

            stompClient.topic("/topic/family/$familyId").subscribe { message: StompMessage ->
                // Giả lập parse JSON. Trong thực tế dùng Gson/Moshi
                val incoming = ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    text = message.payload,
                    isMe = false, // Mock
                    senderName = "Thành viên",
                    timestamp = System.currentTimeMillis()
                )
                messages = listOf(incoming) + messages // Prepend for inverted list
            }
        } catch (e: Exception) {
            Log.e("ChatScreen", "STOMP Connection failed: ${e.message}")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stompClient.disconnect()
        }
    }

    fun sendMessage() {
        if (inputText.isBlank()) return
        
        val newMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = inputText,
            isMe = true,
            senderName = "Tôi",
            timestamp = System.currentTimeMillis()
        )
        // Lạc quan UI (Optimistic UI)
        messages = listOf(newMsg) + messages
        
        try {
            if (isConnected) {
                stompClient.send("/app/chat/$familyId", inputText).subscribe()
            }
        } catch (e: Exception) {
            Log.e("ChatScreen", "STOMP Send failed: ${e.message}")
        }
        
        inputText = ""
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(familyName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(
                                if (isConnected) "🟢 Đã kết nối" else "🔴 Mất kết nối",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                Divider(color = Color(0xFFE2E8F0))
            }
        },
        bottomBar = {
            Column {
                Divider(color = Color(0xFFF1F5F9))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Nhập tin nhắn...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage() },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            reverseLayout = true,
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(msg)
            }
            
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
                        Text("Chưa có tin nhắn nào", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.padding(top = 10.dp))
                        Text("Hãy gửi lời chào đầu tiên đến tổ ấm của bạn!", fontSize = 14.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = formatter.format(Date(msg.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!msg.isMe) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(msg.senderName.take(1), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            if (!msg.isMe) {
                Text(msg.senderName, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
            }
            
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (msg.isMe) 20.dp else 4.dp,
                            bottomEnd = if (msg.isMe) 4.dp else 20.dp
                        )
                    )
                    .background(if (msg.isMe) Color(0xFF1A73E8) else Color(0xFFF1F5F9))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
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
