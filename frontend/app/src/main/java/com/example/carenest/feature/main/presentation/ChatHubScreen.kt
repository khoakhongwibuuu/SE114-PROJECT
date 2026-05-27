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
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.dashboard.presentation.DashboardState
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel

private enum class ChatHubTab(val label: String) {
    FAMILY("Tổ ấm"),
    AI("AI Care"),
}

@Composable
fun ChatHubScreen(
    dashboardViewModel: DashboardViewModel,
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
            ChatHubTab.AI -> AiCarePane()
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
private fun AiCarePane() {
    val prompts = listOf(
        "Hôm nay cần uống thuốc gì?",
        "Thuốc nào sắp hết hạn?",
        "Tóm tắt sức khỏe của gia đình",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFE0F2FE), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue)
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("CareNest AI", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        Text("Trợ lý sức khỏe cho gia đình bạn", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Bạn có thể hỏi nhanh về lịch thuốc, tủ thuốc, hoặc tóm tắt tình hình sức khỏe hiện tại.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color(0xFF334155),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        prompts.forEach { prompt ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
            ) {
                Text(
                    text = prompt,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                )
            }
        }
    }
}
