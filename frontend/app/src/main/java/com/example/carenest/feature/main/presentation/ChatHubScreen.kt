package com.example.carenest.feature.main.presentation

import com.example.carenest.feature.chat.domain.model.ChatGroup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.components.CareNestIcon
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.AppSpacing
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.chat.presentation.AiChatViewModel

private enum class ChatHubTab(val label: String) {
    AI("AI Care"),
    DOCTOR("Bác sĩ"),
}

@Composable
fun ChatHubScreen(
    aiChatViewModel: AiChatViewModel,
    onNavigateToAppointments: () -> Unit
) {
    var activeTab by remember { mutableStateOf(ChatHubTab.AI) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground),
        ) {
            ChatHubTab.entries.forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = tab }
                        .padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        style = CareNestTextStyles.labelMd,
                        color = if (activeTab == tab) PrimaryBlue else Outline,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(if (activeTab == tab) PrimaryBlue else Color.Transparent),
                    )
                }
            }
        }

        when (activeTab) {
            ChatHubTab.AI -> AiCarePane(aiChatViewModel)
            ChatHubTab.DOCTOR -> DoctorMessagingPlaceholder(onNavigateToAppointments = onNavigateToAppointments)
        }
    }
}

@Composable
private fun DoctorMessagingPlaceholder(onNavigateToAppointments: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground)
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CareNestIcon(
            name = "chat",
            contentDescription = "Bác sĩ",
            tint = PrimaryBlue,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = "Tư vấn trực tiếp với Bác sĩ",
            style = CareNestTextStyles.titleMd,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = "Tính năng trò chuyện trực tiếp với Bác sĩ chuyên khoa đang được phát triển và sẽ sớm ra mắt.\n\nĐể nhận tư vấn y tế trực tiếp từ các bác sĩ đối tác của CareNest, bạn có thể đặt lịch hẹn khám tại đây.",
            style = CareNestTextStyles.bodyMd.copy(lineHeight = 22.sp),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AppSpacing.lg)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        Button(
            onClick = onNavigateToAppointments,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(AppRadius.md),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Đặt lịch hẹn khám",
                style = CareNestTextStyles.labelMd,
                color = Color.White
            )
        }
    }
}

@Composable
private fun AiCarePane(viewModel: AiChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBackground)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg),
            contentPadding = PaddingValues(vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            items(messages, key = { "${it.isUser}-${it.text.hashCode()}" }) { msg ->
                val align = if (msg.isUser) Alignment.End else Alignment.Start
                val bg = if (msg.isUser) PrimaryBlue else Color(0xFFF1F5F9)
                val textColor = if (msg.isUser) Color.White else TextPrimary

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = align
                ) {
                    Box(
                        modifier = Modifier
                            .background(bg, RoundedCornerShape(AppRadius.xl))
                            .padding(horizontal = AppSpacing.lg, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = textColor,
                            style = CareNestTextStyles.bodyMd.copy(lineHeight = 22.sp)
                        )
                    }
                }
            }
            if (isTyping) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(AppRadius.xl))
                                .padding(horizontal = AppSpacing.lg, vertical = 10.dp)
                        ) {
                            Text(
                                "Đang trả lời...",
                                color = Color(0xFF94A3B8),
                                style = CareNestTextStyles.bodyMd.copy(fontStyle = FontStyle.Italic)
                            )
                        }
                    }
                }
            }

            if (messages.size == 1) {
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    Text(
                        "Gợi ý cho bạn:",
                        style = CareNestTextStyles.labelSm.copy(fontSize = 13.sp),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    val prompts = listOf(
                        "Hôm nay cần uống thuốc gì?",
                        "Thuốc nào sắp hết hạn?",
                        "Tóm tắt sức khỏe của gia đình"
                    )
                    prompts.forEach { prompt ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = PageBackground),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = AppSpacing.sm)
                                .clickable {
                                    viewModel.sendMessage(prompt)
                                }
                        ) {
                            Text(
                                text = prompt,
                                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = 12.dp),
                                style = CareNestTextStyles.labelMd,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = AppSpacing.lg, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Hỏi CareNest AI...",
                        color = Color(0xFF94A3B8),
                        style = CareNestTextStyles.bodyMd
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(AppRadius.full),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (inputText.isNotBlank() && !isTyping) PrimaryBlue else Color(0xFFE2E8F0),
                        CircleShape
                    )
                    .clickable(enabled = inputText.isNotBlank() && !isTyping) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isTyping) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    CareNestIcon(
                        name = "send",
                        contentDescription = "Gửi",
                        tint = if (inputText.isNotBlank()) Color.White else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
