package com.example.carenest.feature.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.core.presentation.components.CareNestIcon
import com.example.carenest.core.presentation.theme.AppElevation
import com.example.carenest.core.presentation.theme.AppRadius
import com.example.carenest.core.presentation.theme.AppSpacing
import com.example.carenest.core.presentation.theme.CareNestTextStyles
import com.example.carenest.core.presentation.theme.CardBackground
import com.example.carenest.core.presentation.theme.Outline
import com.example.carenest.core.presentation.theme.PageBackground
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.PrimaryFixed
import com.example.carenest.core.presentation.theme.SurfaceHigh
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.dashboard.domain.model.DashboardTask
import com.example.carenest.feature.dashboard.domain.model.Family
import com.example.carenest.feature.dashboard.domain.model.Member
import com.example.carenest.feature.dashboard.presentation.DashboardState
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToMedicine: () -> Unit,
    onNavigateToAppointment: () -> Unit,
    onNavigateToVaccine: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToTask: (DashboardTask) -> Unit
) {
    val uiState by viewModel.dashboardState.collectAsState()
    val selectedMemberId by viewModel.selectedMemberId.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentFamilyId by viewModel.currentFamilyId.collectAsState()
    val scrollState = rememberScrollState()
    var showFamilySwitcher by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchDashboard()
    }

    when (val state = uiState) {
        is DashboardState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }

        is DashboardState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = state.error,
                    style = CareNestTextStyles.bodyMd,
                    color = Color(0xFFEF4444),
                    textAlign = TextAlign.Center
                )
            }
        }

        is DashboardState.Success -> {
            val dashboard = state.data
            val activeFamily = dashboard.families.firstOrNull { it.id == currentFamilyId } ?: dashboard.families.firstOrNull()

            Scaffold(
                containerColor = PageBackground
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PageBackground)
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xl / 2)
                ) {
                    HomeHeader(
                        activeFamilyName = activeFamily?.name ?: "CareNest",
                        hasMultipleFamilies = dashboard.families.size > 1,
                        unreadCount = state.unreadCount,
                        onOpenSwitcher = { showFamilySwitcher = true },
                        onNavigateToNotifications = onNavigateToNotifications
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.x2))

                    GreetingSection(currentUser?.fullName ?: "bạn")

                    Spacer(modifier = Modifier.height(AppSpacing.x2))

                    MemberSelector(
                        members = dashboard.members,
                        selectedId = selectedMemberId,
                        onSelect = { viewModel.selectMember(it) }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.x2))

                    ShortcutGrid(
                        onMed = onNavigateToMedicine,
                        onAppt = onNavigateToAppointment,
                        onVac = onNavigateToVaccine
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.x2))

                    HeroCard(
                        memberCount = dashboard.members.size,
                        unreadCount = state.unreadCount,
                        medCount = state.tasks.count { it.type == "MEDICATION" }
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.x2))

                    TasksSection(
                        tasks = state.tasks,
                        onTaskClick = onNavigateToTask
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.x2))

                    AiAdvisorCard(text = state.aiSummaryText)

                    Spacer(modifier = Modifier.height(AppSpacing.x8))
                }

                if (showFamilySwitcher) {
                    ModalBottomSheet(
                        onDismissRequest = { showFamilySwitcher = false },
                        containerColor = CardBackground,
                        shape = RoundedCornerShape(topStart = AppRadius.x2 + 4.dp, topEnd = AppRadius.x2 + 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(AppSpacing.x2).padding(bottom = AppSpacing.x4)) {
                            Text(
                                "Chọn gia đình",
                                style = CareNestTextStyles.titleLg,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = AppSpacing.md)
                            )
                            dashboard.families.forEach { family ->
                                val isActive = family.id == (activeFamily?.id ?: "")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = AppSpacing.xs)
                                        .clip(RoundedCornerShape(AppRadius.xl))
                                        .background(if (isActive) PrimaryFixed else Color.Transparent)
                                        .clickable {
                                            viewModel.switchFamily(family)
                                            showFamilySwitcher = false
                                        }
                                        .padding(horizontal = AppSpacing.lg, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            family.name,
                                            style = CareNestTextStyles.labelLg.copy(fontSize = 15.sp),
                                            color = if (isActive) PrimaryBlue else TextPrimary
                                        )
                                        Text(
                                            "Thành viên",
                                            style = CareNestTextStyles.bodySm,
                                            color = TextSecondary
                                        )
                                    }
                                    if (isActive) {
                                        Text("✓", style = CareNestTextStyles.titleLg, color = PrimaryBlue)
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
    onOpenSwitcher: () -> Unit,
    onNavigateToNotifications: () -> Unit
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
            Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(activeFamilyName, style = CareNestTextStyles.titleXl.copy(fontSize = 22.sp), color = PrimaryBlue)
            if (hasMultipleFamilies) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PrimaryBlue)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Outline, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onNavigateToNotifications() }
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Thông báo",
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(28.dp)
                )
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
        Text("Xin chào, $name!", style = CareNestTextStyles.headlineLg.copy(fontSize = 26.sp), color = TextPrimary)
        Text(
            "Hy vọng gia đình mình có một ngày khỏe mạnh.",
            style = CareNestTextStyles.bodyMd,
            color = TextSecondary,
            modifier = Modifier.padding(top = AppSpacing.xs)
        )
    }
}

@Composable
fun MemberSelector(members: List<Member>, selectedId: String?, onSelect: (String?) -> Unit) {
    Column {
        Text("THÀNH VIÊN", style = CareNestTextStyles.overline, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(AppSpacing.md))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
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
            .clip(RoundedCornerShape(AppRadius.full))
            .background(if (isActive) PrimaryBlue else Color(0xFFF1F5F9))
            .clickable { onClick() }
            .padding(horizontal = AppSpacing.xl, vertical = 10.dp)
    ) {
        Text(
            name,
            style = CareNestTextStyles.labelMd,
            color = if (isActive) Color.White else Color(0xFF475569)
        )
    }
}
@Composable
fun ShortcutGrid(onMed: () -> Unit, onAppt: () -> Unit, onVac: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), modifier = Modifier.fillMaxWidth()) {
        ShortcutCard("L\u1ECBch thu\u1ED1c", "pill", 0xFF0EA5E9, 0xFFE0F2FE, Modifier.weight(1f), onMed)
        ShortcutCard("L\u1ECBch h\u1EB9n", "calendar_month", 0xFFA855F7, 0xFFF3E8FF, Modifier.weight(1f), onAppt)
        ShortcutCard("Ti\u00EAm ch\u1EE7ng", "syringe", 0xFF0097A7, 0xFFE0F7FA, Modifier.weight(1f), onVac)
    }
}

@Composable
fun ShortcutCard(label: String, iconName: String, iconColor: Long, bgColor: Long, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(AppRadius.x2),
        color = CardBackground,
        shadowElevation = AppElevation.sm,
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(bgColor)),
                contentAlignment = Alignment.Center
            ) {
                CareNestIcon(name = iconName, contentDescription = null, tint = Color(iconColor), modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(AppSpacing.sm + 2.dp))
            Text(label, style = CareNestTextStyles.labelSm, color = TextPrimary)
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
            .padding(AppSpacing.x2)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Hôm nay", style = CareNestTextStyles.bodyMd, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        if (unreadCount > 0) "Có việc cần chú ý" else "Mọi thứ đều ổn",
                        style = CareNestTextStyles.headlineLg,
                        color = Color.White
                    )
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
        Text(label.uppercase(), style = CareNestTextStyles.labelSm.copy(fontSize = 9.sp), color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(value, style = CareNestTextStyles.labelMd, color = Color.White)
    }
}

@Composable
fun TasksSection(tasks: List<DashboardTask>, onTaskClick: (DashboardTask) -> Unit) {
    Column {
        Text("HÔM NAY CẦN LÀM", style = CareNestTextStyles.overline, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(AppSpacing.md))

        if (tasks.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadius.x2),
                color = CardBackground,
                shadowElevation = AppElevation.sm,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(modifier = Modifier.padding(AppSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(PrimaryFixed), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.lg))
                    Column {
                        Text("Chưa có việc nào cần xử lý", style = CareNestTextStyles.labelLg.copy(fontSize = 15.sp), color = TextPrimary)
                        Text("Dashboard sẽ tự cập nhật khi có lịch mới.", style = CareNestTextStyles.bodySm.copy(fontSize = 13.sp), color = TextSecondary)
                    }
                }
            }
        } else {
            tasks.forEach { task ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.md).clickable { onTaskClick(task) },
                    shape = RoundedCornerShape(AppRadius.x2),
                    color = CardBackground,
                    shadowElevation = AppElevation.sm,
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(modifier = Modifier.padding(AppSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Color(task.iconBgColor)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(task.iconColor), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.lg))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = CareNestTextStyles.labelLg.copy(fontSize = 15.sp), color = TextPrimary)
                            Text(task.subtitle, style = CareNestTextStyles.bodySm.copy(fontSize = 13.sp), color = TextSecondary)
                        }
                        if (task.badge != null) {
                            val isTomorrow = task.badge.contains("Ngày mai")
                            val isToday = task.badge.contains("Hôm nay")
                            val badgeBg = if (isTomorrow) Color(0xFFFFEDD5) else if (isToday) Color(0xFFFEE2E2) else Color(0xFFEEF2FF)
                            val badgeColor = if (isTomorrow) Color(0xFFF97316) else if (isToday) Color(0xFFEF4444) else Color(0xFF4F46E5)

                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(badgeBg).padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Text(task.badge, style = CareNestTextStyles.labelSm.copy(fontSize = 11.sp), color = badgeColor)
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
        shape = RoundedCornerShape(AppRadius.x2),
        color = Color(0xFFE1F5FE),
        border = BorderStroke(1.dp, Color(0xFF0047AB).copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(AppSpacing.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(AppRadius.lg)).background(Color(0xFF0047AB)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(AppSpacing.sm + 2.dp))
                Text("AI CỐ VẤN", style = CareNestTextStyles.overline.copy(letterSpacing = 1.sp), color = Color(0xFF0047AB))
            }
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text("\"$text\"", style = CareNestTextStyles.bodyMd.copy(fontStyle = FontStyle.Italic, lineHeight = 22.sp), color = TextPrimary)
        }
    }
}
