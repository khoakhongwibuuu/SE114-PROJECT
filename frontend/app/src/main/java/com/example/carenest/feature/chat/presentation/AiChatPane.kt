package com.example.carenest.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun AiChatPane() {
    val application = LocalContext.current.applicationContext as CareNestApplication
    val viewModel: AiChatViewModel = viewModel(
        factory = AiChatViewModelFactory(application.aiChatApi)
    )
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                AiMessageBubble(message = message)
            }
            if (isTyping) {
                item {
                    AiMessageBubble(
                        message = AiMessage("CareNest AI đang trả lời...", isUser = false, isLoading = true)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập câu hỏi sức khỏe") },
                maxLines = 4,
                enabled = !isTyping
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val message = input
                    input = ""
                    viewModel.sendMessage(message)
                },
                enabled = input.isNotBlank() && !isTyping
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gửi",
                    tint = if (input.isNotBlank() && !isTyping) PrimaryBlue else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AiMessageBubble(message: AiMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) PrimaryBlue else CardBackground
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (message.isUser) "Bạn" else "CareNest AI",
                    style = CareNestTextStyles.labelSm,
                    color = if (message.isUser) Color.White.copy(alpha = 0.85f) else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message.text,
                    style = CareNestTextStyles.bodyMd,
                    color = if (message.isUser) Color.White else TextPrimary
                )
            }
        }
    }
}
