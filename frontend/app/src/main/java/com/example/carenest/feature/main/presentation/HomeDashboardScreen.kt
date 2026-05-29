package com.example.carenest.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.R
import com.example.carenest.model.DashboardTask
import com.example.carenest.model.Family
import com.example.carenest.model.Member
import com.example.carenest.viewmodel.DashboardState
import com.example.carenest.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToMedicine: () -> Unit,
    onNavigateToAppointment: () -> Unit,
    onNavigateToVaccine: () -> Unit,
    onNavigateToTask: (DashboardTask) -> Unit
) {
    val uiState by viewModel.dashboardState.collectAsState()
    val selectedMemberId by viewModel.selectedMemberId.collectAsState()
    val scrollState = rememberScrollState()
    var showFamilySwitcher by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchDashboard()
    }

    when (val state = uiState) {
        is DashboardState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0047AB))
            }
        }
        is DashboardState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error, color = Color.Red)
            }
        }
        is DashboardState.Success -> {
            val dashboard = state.data
            val activeFamily = dashboard.families.firstOrNull() // Simplify: just pick first as active in this mock
            
            Scaffold(
                containerColor = Color.White
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    HomeHeader(
                        activeFamilyName = activeFamily?.name ?: "CareNest",
                        hasMultipleFamilies = dashboard.families.size > 1,
                        unreadCount = state.unreadCount,
                        onOpenSwitcher = { showFamilySwitcher = true }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    GreetingSection("Bạn")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    MemberSelector(
                        members = dashboard.members,
                        selectedId = selectedMemberId,
                        onSelect = { viewModel.selectMember(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    ShortcutGrid(
                        onMed = onNavigateToMedicine,
                        onAppt = onNavigateToAppointment,
                        onVac = onNavigateToVaccine
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HeroCard(
                        memberCount = dashboard.members.size,
                        unreadCount = state.unreadCount,
                        medCount = state.tasks.count { it.type == com.example.carenest.model.TaskType.MEDICATION }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    TasksSection(
                        tasks = state.tasks,
                        onTaskClick = onNavigateToTask
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    AiAdvisorCard(text = state.aiSummaryText)
                    
                    Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
                }

                if (showFamilySwitcher) {
                    ModalBottomSheet(
                        onDismissRequest = { showFamilySwitcher = false },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp).padding(bottom = 40.dp)) {
                            Text("Chọn gia đình", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 12.dp))
                            dashboard.families.forEach { family ->
                                val isActive = family.id == (activeFamily?.id ?: "")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isActive) Color(0xFFEFF6FF) else Color.Transparent)
                                        .clickable { 
                                            viewModel.switchFamily(family)
                                            showFamilySwitcher = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(family.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color(0xFF0047AB) else Color(0xFF1E293B))
                                        Text("Thành viên", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    if (isActive) {
                                        Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0047AB))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    activeFamilyName: String,
    hasMultipleFamilies: Boolean,
    unreadCount: Int,
    onOpenSwitcher: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { if (hasMultipleFamilies) onOpenSwitcher() }
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF0047AB), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(activeFamilyName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0047AB))
            if (hasMultipleFamilies) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF0047AB))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(28.dp))
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingSection(name: String) {
    Column {
        Text("Xin chào, $name!", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
        Text("Hy vọng gia đình mình có một ngày khỏe mạnh.", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun MemberSelector(members: List<Member>, selectedId: String?, onSelect: (String?) -> Unit) {
    Column {
        Text("THÀNH VIÊN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF94A3B8), letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                MemberPill("Cả nhà", selectedId == null) { onSelect(null) }
            }
            items(members) { member ->
                MemberPill(member.name, selectedId == member.id) { onSelect(member.id) }
            }
        }
    }
}

@Composable
fun MemberPill(name: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(if (isActive) Color(0xFF0047AB) else Color(0xFFF1F5F9))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else Color(0xFF475569))
    }
}

@Composable
fun ShortcutGrid(onMed: () -> Unit, onAppt: () -> Unit, onVac: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        ShortcutCard("Lịch thuốc", 0xFF0EA5E9, 0xFFE0F2FE, Modifier.weight(1f), onMed)
        ShortcutCard("Lịch hẹn", 0xFFA855F7, 0xFFF3E8FF, Modifier.weight(1f), onAppt)
        ShortcutCard("Tiêm chủng", 0xFF0097A7, 0xFFE0F7FA, Modifier.weight(1f), onVac)
    }
}

@Composable
fun ShortcutCard(label: String, iconColor: Long, bgColor: Long, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(bgColor)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(iconColor), modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun HeroCard(memberCount: Int, unreadCount: Int, medCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF007BFF), Color(0xFF0047AB))))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Hôm nay", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(if (unreadCount > 0) "Có việc cần chú ý" else "Mọi thứ đều ổn", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassModule("Thành viên", memberCount.toString(), Modifier.weight(1f))
                GlassModule("Nhắc nhở", unreadCount.toString(), Modifier.weight(1f))
                GlassModule("Thuốc hôm nay", medCount.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun GlassModule(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label.uppercase(), fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun TasksSection(tasks: List<DashboardTask>, onTaskClick: (DashboardTask) -> Unit) {
    Column {
        Text("HÔM NAY CẦN LÀM", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF94A3B8), letterSpacing = 1.2.sp)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (tasks.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFEFF6FF)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Chưa có việc nào cần xử lý", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Dashboard sẽ tự cập nhật khi có lịch mới.", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
            }
        } else {
            tasks.forEach { task ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onTaskClick(task) },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Color(task.iconBgColor)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(task.iconColor), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text(task.subtitle, fontSize = 13.sp, color = Color(0xFF64748B))
                        }
                        if (task.badge != null) {
                            val isTomorrow = task.badge.contains("Ngày mai")
                            val isToday = task.badge.contains("Hôm nay")
                            val badgeBg = if (isTomorrow) Color(0xFFFFEDD5) else if (isToday) Color(0xFFFEE2E2) else Color(0xFFEEF2FF)
                            val badgeColor = if (isTomorrow) Color(0xFFF97316) else if (isToday) Color(0xFFEF4444) else Color(0xFF4F46E5)
                            
                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(badgeBg).padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Text(task.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                            }
                        } else {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAdvisorCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFE1F5FE),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0047AB).copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0047AB)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("AI CỐ VẤN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0047AB), letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("\"$text\"", fontSize = 14.sp, color = Color(0xFF1E293B), fontStyle = FontStyle.Italic, lineHeight = 22.sp)
        }
    }
}
