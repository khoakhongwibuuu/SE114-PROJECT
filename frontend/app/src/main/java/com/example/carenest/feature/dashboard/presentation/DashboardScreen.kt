package com.example.carenest.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.R
import com.example.carenest.feature.dashboard.domain.model.Family
import com.example.carenest.feature.dashboard.domain.model.Member
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.core.presentation.theme.TextPrimary
import com.example.carenest.core.presentation.theme.TextSecondary
import com.example.carenest.feature.dashboard.presentation.DashboardState
import com.example.carenest.feature.dashboard.presentation.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val currentFamilyId by viewModel.currentFamilyId.collectAsState()
    var showFamilySheet by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = TextSecondary)
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notification", tint = TextSecondary)
                    }
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable { showFamilySheet = true }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.carenest_logo_house),
                            contentDescription = "Logo",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val familyName = if (dashboardState is DashboardState.Success) {
                            val data = (dashboardState as DashboardState.Success).data
                            data.families.find { it.id == currentFamilyId }?.name ?: "CareNest"
                        } else "CareNest"
                        
                        Text(
                            text = familyName,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Icon(Icons.Default.ExpandMore, contentDescription = "Switch Family", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (dashboardState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
                }
                is DashboardState.Error -> {
                    Text(
                        text = (dashboardState as DashboardState.Error).error,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is DashboardState.Success -> {
                    val data = (dashboardState as DashboardState.Success).data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        // Greeting
                        Text(
                            text = "Xin chÃ o, báº¡n!",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Hy vá»ng gia Ä‘Ã¬nh mÃ¬nh cÃ³ má»™t ngÃ y khá»e máº¡nh.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        // Members
                        Text(
                            text = "THÃ€NH VIÃŠN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            item {
                                MemberPill(
                                    name = "Cáº£ nhÃ ",
                                    isActive = selectedMemberId == null,
                                    onClick = { selectedMemberId = null }
                                )
                            }
                            items(data.members) { member ->
                                MemberPill(
                                    name = member.name.split(" ").last(),
                                    isActive = selectedMemberId == member.id,
                                    onClick = { selectedMemberId = member.id }
                                )
                            }
                        }

                        // Shortcuts
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            ShortcutCard(icon = Icons.Default.MedicalServices, label = "Lá»‹ch thuá»‘c", iconBg = Color(0xFFE0F2FE), iconTint = Color(0xFF0EA5E9), modifier = Modifier.weight(1f))
                            ShortcutCard(icon = Icons.Default.CalendarMonth, label = "Lá»‹ch háº¹n", iconBg = Color(0xFFF3E8FF), iconTint = Color(0xFFA855F7), modifier = Modifier.weight(1f))
                            ShortcutCard(icon = Icons.Default.Vaccines, label = "TiÃªm chá»§ng", iconBg = Color(0xFFE0F7FA), iconTint = Color(0xFF0097A7), modifier = Modifier.weight(1f))
                        }

                        // Hero Card
                        HeroCard(memberCount = data.members.size, unreadCount = 2, medCount = data.medications.size)

                        // Tasks
                        Text(
                            text = "HÃ”M NAY Cáº¦N LÃ€M",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        
                        if (data.medications.isEmpty() && data.appointments.isEmpty()) {
                            TaskCard(
                                icon = Icons.Default.CheckCircle,
                                title = "ChÆ°a cÃ³ viá»‡c nÃ o cáº§n xá»­ lÃ½",
                                subtitle = "Dashboard sáº½ tá»± cáº­p nháº­t khi cÃ³ lá»‹ch thuá»‘c, khÃ¡m hoáº·c tiÃªm chá»§ng.",
                                iconBg = Color(0xFFEFF6FF),
                                iconTint = Color(0xFF2563EB)
                            )
                        } else {
                            data.medications.forEach { med ->
                                TaskCard(
                                    icon = Icons.Default.MedicalServices,
                                    title = med.name,
                                    subtitle = med.time,
                                    iconBg = Color(0xFFEFF6FF),
                                    iconTint = Color(0xFF2563EB),
                                    badge = if (med.isTaken) "ÄÃƒ Uá»NG" else "CHÆ¯A Uá»NG"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // AI Advisor
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE1F5FE))
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.carenest_logo_house),
                                        contentDescription = "AI",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Cá» Váº¤N", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "HÃ´m nay cáº£ nhÃ  cÃ³ ${data.medications.size} viá»‡c cáº§n chÃº Ã½. HÃ£y lÆ°u Ã½ chuáº©n bá»‹ Ä‘áº§y Ä‘á»§ nhÃ©!",
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    if (showFamilySheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showFamilySheet = false },
                            sheetState = sheetState,
                            containerColor = Color.White
                        ) {
                            Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                                Text("Chá»n gia Ä‘Ã¬nh", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(Modifier.height(16.dp))
                                data.families.forEach { family ->
                                    val isActive = family.id == currentFamilyId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isActive) Color(0xFFEFF6FF) else Color.Transparent)
                                            .clickable {
                                                viewModel.switchFamily(family)
                                                showFamilySheet = false
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(family.name, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = if (isActive) PrimaryBlue else TextPrimary, fontSize = 16.sp)
                                            Text("ThÃ nh viÃªn", color = TextSecondary, fontSize = 12.sp)
                                        }
                                        if (isActive) {
                                            Icon(Icons.Default.Check, contentDescription = "Active", tint = PrimaryBlue)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberPill(name: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(if (isActive) PrimaryBlue else Color(0xFFF1F5F9))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = name,
            color = if (isActive) Color.White else Color(0xFF475569),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ShortcutCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, iconBg: Color, iconTint: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("HÃ´m nay", color = Color(0xB3FFFFFF), fontSize = 14.sp)
                    Text("CÃ³ viá»‡c cáº§n chÃº Ã½", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                }
                Icon(Icons.Default.WbSunny, contentDescription = "Weather", tint = Color(0xCCFFFFFF), modifier = Modifier.size(40.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassModule(icon = Icons.Default.Group, label = "ThÃ nh viÃªn", value = memberCount.toString(), modifier = Modifier.weight(1f))
                GlassModule(icon = Icons.Default.Notifications, label = "Nháº¯c nhá»Ÿ", value = unreadCount.toString(), modifier = Modifier.weight(1f))
                GlassModule(icon = Icons.Default.MedicalServices, label = "Thuá»‘c hÃ´m nay", value = medCount.toString(), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun GlassModule(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x33FFFFFF))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color(0xCCFFFFFF), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TaskCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, iconBg: Color, iconTint: Color, badge: String? = null) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 14.sp, color = TextSecondary)
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (badge.contains("ÄÃƒ Uá»NG")) Color(0xFFF0FDF4) else Color(0xFFEFF6FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.contains("ÄÃƒ Uá»NG")) Color(0xFF16A34A) else Color(0xFF2563EB)
                    )
                }
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
            }
        }
    }
}
