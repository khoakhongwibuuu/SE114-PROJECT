package com.example.carenest.feature.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Send
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.dashboard.presentation.DashboardState
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel
import com.example.carenest.feature.chat.presentation.AiChatViewModel

private enum class ChatHubTab(val label: String) {
    FAMILY("Tổ ấm"),
    AI("AI Care"),
}

@Composable
fun ChatHubScreen(
    dashboardViewModel: DashboardViewModel,
    aiChatViewModel: AiChatViewModel
) {
    var activeTab by remember { mutableStateOf(ChatHubTab.FAMILY) }
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (activeTab == tab) PrimaryBlue else Color(0xFF707882),
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
            ChatHubTab.FAMILY -> FamilyHubPane(dashboardState)
            ChatHubTab.AI -> AiCarePane(aiChatViewModel)
        }
    }
}

@Composable
private fun FamilyHubPane(
    dashboardState: DashboardState,
) {
    val families = (dashboardState as? DashboardState.Success)?.data?.families.orEmpty()
    if (families.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bạn chưa thuộc gia đình nào.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Vào tab Gia đình để tạo hoặc tham gia một tổ ấm nhé!",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(families, key = { it.id }) { family ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFE0F2FE), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                            Text(family.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bấm để bắt đầu trò chuyện", fontSize = 14.sp, color = Color(0xFF64748B))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1))
                    }
                }
            }
        }
    }
}

@Composable
private fun AiCarePane(viewModel: AiChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var inputText by remember { mutableStateOf("") }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val align = if (msg.isUser) Alignment.End else Alignment.Start
                val bg = if (msg.isUser) PrimaryBlue else Color(0xFFF1F5F9)
                val textColor = if (msg.isUser) Color.White else Color(0xFF1E293B)
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = align
                ) {
                    Box(
                        modifier = Modifier
                            .background(bg, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(text = msg.text, color = textColor, fontSize = 15.sp, lineHeight = 22.sp)
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
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Đang trả lời...", color = Color(0xFF94A3B8), fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }
            }
            
            if (messages.size == 1) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Gợi ý cho bạn:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    val prompts = listOf(
                        "Hôm nay cần uống thuốc gì?",
                        "Thuốc nào sắp hết hạn?",
                        "Tóm tắt sức khỏe của gia đình"
                    )
                    prompts.forEach { prompt ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clickable {
                                    viewModel.sendMessage(prompt)
                                }
                        ) {
                            Text(
                                text = prompt,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                fontSize = 14.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        
        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Hỏi CareNest AI...", color = Color(0xFF94A3B8)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F5F9),
                    unfocusedContainerColor = Color(0xFFF1F5F9),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (inputText.isNotBlank() && !isTyping) PrimaryBlue else Color(0xFFE2E8F0), CircleShape)
                    .clickable(enabled = inputText.isNotBlank() && !isTyping) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isTyping) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryBlue, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", tint = if (inputText.isNotBlank()) Color.White else Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
