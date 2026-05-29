package com.example.carenest.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.R
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.dashboard.domain.model.Appointment
import com.example.carenest.feature.dashboard.domain.model.DashboardTask
import com.example.carenest.feature.dashboard.domain.model.Family
import com.example.carenest.feature.dashboard.domain.model.Medication
import com.example.carenest.feature.dashboard.domain.model.Member
import com.example.carenest.feature.dashboard.domain.model.Vaccine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

private val HeaderBlue = Color(0xFF0047AB)
private val PageBackground = Color.White
private val SectionOverline = Color(0xFF94A3B8)
private val InactivePill = Color(0xFFF1F5F9)
private val HeroStart = Color(0xFF007BFF)
private val HeroEnd = Color(0xFF0047AB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToMedicineSchedule: () -> Unit = {},
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToVaccinations: (Long) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dashboardState by viewModel.dashboardState.collectAsState()
    val currentFamilyId by viewModel.currentFamilyId.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showFamilySheet by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val onNavigateToVaccination = {
        val profileId = currentFamilyId?.toLongOrNull() ?: 1L
        onNavigateToVaccinations(profileId)
    }
    val onNavigateToNotifications = {
        Toast.makeText(context, "Tính năng thông báo đang được phát triển", Toast.LENGTH_SHORT).show()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground),
    ) {
        when (val state = dashboardState) {
            DashboardState.Loading -> {
                CircularProgressIndicator(
                    color = PrimaryBlue,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            is DashboardState.Error -> {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )
            }

            is DashboardState.Success -> {
                val data = state.data
                val activeFamilyName = data.families.firstOrNull { it.id == currentFamilyId }?.name
                    ?: data.families.firstOrNull()?.name
                    ?: "CareNest"
                val tasks = buildDashboardTasks(
                    tasks = data.todayTasks,
                    medications = data.medications,
                    appointments = data.appointments,
                    vaccines = data.vaccines,
                )
                val targetLabel = selectedMemberText(selectedMemberId, data.members)
                val medicineCount = tasks.count { it.icon == Icons.Default.MedicalServices }
                val userName = currentUser?.fullName ?: "bạn"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp, bottom = 20.dp)
                        .background(Color.White),
                ) {
                    DashboardHeader(
                        familyName = activeFamilyName,
                        unreadCount = data.unreadNotifications.toInt(),
                        onOpenSwitcher = { showFamilySheet = true },
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToNotifications = onNavigateToNotifications,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GreetingSection(userName = userName)

                    SectionTitle("THÀNH VIÊN")
                    MemberRow(
                        members = data.members,
                        selectedMemberId = selectedMemberId,
                        onSelect = { selectedMemberId = it },
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ShortcutRow(
                        onNavigateToMedicineSchedule = onNavigateToMedicineSchedule,
                        onNavigateToAppointments = onNavigateToAppointments,
                        onNavigateToVaccination = onNavigateToVaccination,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HeroCard(
                        status = if (data.unreadNotifications > 0) "Có việc cần chú ý" else "Mọi thứ đều ổn",
                        memberCount = data.members.size,
                        unreadCount = data.unreadNotifications.toInt(),
                        medicineCount = medicineCount,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle("HÔM NAY CẦN LÀM", bottomPadding = 0.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (tasks.isEmpty()) {
                        TaskRow(
                            icon = Icons.Default.CheckCircle,
                            iconBg = Color(0xFFEFF6FF),
                            iconTint = Color(0xFF2563EB),
                            title = "Chưa có việc nào cần xử lý",
                            subtitle = "Dashboard sẽ tự cập nhật khi có lịch thuốc, khám hoặc tiêm chủng.",
                        )
                    } else {
                        tasks.forEachIndexed { index, task ->
                            TaskRow(
                                icon = task.icon,
                                iconBg = task.iconBg,
                                iconTint = task.iconTint,
                                title = task.title,
                                subtitle = task.subtitle,
                                badge = task.badge,
                            )
                            if (index != tasks.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AiAdvisorCard(
                        summary = if (tasks.isEmpty()) {
                            "Hôm nay chưa có cảnh báo lớn. Bạn có thể kiểm tra lịch thuốc, lịch khám và hỏi CareNest AI nếu cần tra cứu nhanh."
                        } else {
                            "Hôm nay $targetLabel có ${tasks.size} việc cần chú ý thực hiện. Hãy lưu ý chuẩn bị đầy đủ nhé!"
                        },
                    )
                }

                if (showFamilySheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showFamilySheet = false },
                        sheetState = sheetState,
                        containerColor = Color.White,
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                            Text(
                                text = "Chọn gia đình",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            data.families.forEach { family ->
                                FamilyRow(
                                    family = family,
                                    active = family.id == currentFamilyId,
                                    onClick = {
                                        viewModel.switchFamily(family)
                                        selectedMemberId = null
                                        showFamilySheet = false
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    familyName: String,
    unreadCount: Int,
    onOpenSwitcher: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onOpenSwitcher),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.carenest_logo_house),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = familyName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HeaderBlue,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = HeaderBlue)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCFE5FF))
                    .clickable(onClick = onNavigateToProfile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = HeaderBlue,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = onNavigateToNotifications) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Thông báo",
                        tint = Color(0xFF64748B),
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                    )
                }
            }
        }
    }
}

@Composable
private fun GreetingSection(userName: String) {
    Text(
        text = "Xin chào, $userName!",
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1E293B),
    )
    Text(
        text = "Hy vọng gia đình mình có một ngày khỏe mạnh.",
        fontSize = 14.sp,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
    )
}

@Composable
private fun SectionTitle(text: String, bottomPadding: Dp = 12.dp) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = SectionOverline,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(bottom = bottomPadding),
    )
}

@Composable
private fun MemberRow(
    members: List<Member>,
    selectedMemberId: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MemberPill(
            name = "Cả nhà",
            active = selectedMemberId == null,
            onClick = { onSelect(null) },
        )
        members.forEach { member ->
            val displayName = member.name.trim().split(" ").lastOrNull()?.ifBlank { "Thành viên" } ?: "Thành viên"
            MemberPill(
                name = displayName,
                active = selectedMemberId == member.id,
                onClick = {
                    onSelect(if (selectedMemberId == member.id) null else member.id)
                },
            )
        }
    }
}

@Composable
private fun MemberPill(name: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp))
            .background(if (active) HeaderBlue else InactivePill)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) Color.White else Color(0xFF475569),
        )
    }
}

@Composable
private fun ShortcutRow(
    onNavigateToMedicineSchedule: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToVaccination: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShortcutCard(
            icon = Icons.Default.MedicalServices,
            label = "Lịch thuốc",
            iconBg = Color(0xFFE0F2FE),
            iconTint = Color(0xFF0EA5E9),
            modifier = Modifier.weight(1f),
            onClick = onNavigateToMedicineSchedule,
        )
        ShortcutCard(
            icon = Icons.Default.CalendarMonth,
            label = "Lịch hẹn",
            iconBg = Color(0xFFF3E8FF),
            iconTint = Color(0xFFA855F7),
            modifier = Modifier.weight(1f),
            onClick = onNavigateToAppointments,
        )
        ShortcutCard(
            icon = Icons.Default.Vaccines,
            label = "Tiêm chủng",
            iconBg = Color(0xFFE0F7FA),
            iconTint = Color(0xFF0097A7),
            modifier = Modifier.weight(1f),
            onClick = onNavigateToVaccination,
        )
    }
}

@Composable
private fun ShortcutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
            )
        }
    }
}

@Composable
private fun HeroCard(
    status: String,
    memberCount: Int,
    unreadCount: Int,
    medicineCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(HeroStart, HeroEnd)))
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date()),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                    )
                    Text(
                        text = status,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(40.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStat(
                    icon = Icons.Default.Group,
                    label = "Thành viên",
                    value = memberCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                HeroStat(
                    icon = Icons.Default.Notifications,
                    label = "Nhắc nhở",
                    value = unreadCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                HeroStat(
                    icon = Icons.Default.MedicalServices,
                    label = "Thuốc hôm nay",
                    value = medicineCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HeroStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .then(
                Modifier
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private data class DashboardTaskUi(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val badge: String? = null,
)

@Composable
private fun TaskRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (badge != null) {
                // Badge colors extracted 1:1 from legacy RN HomeDashboardScreen.tsx
                val bg = when {
                    badge.contains("Hôm nay", ignoreCase = true) -> Color(0xFFFEE2E2)
                    badge.contains("Ngày mai", ignoreCase = true) -> Color(0xFFFFEDD5)
                    badge.contains("Ngày kia", ignoreCase = true) -> Color(0xFFECFDF5)
                    badge.contains("ĐÃ UỐNG") -> Color(0xFFF0FDF4)
                    else -> Color(0xFFEEF2FF)
                }
                val fg = when {
                    badge.contains("Hôm nay", ignoreCase = true) -> Color(0xFFEF4444)
                    badge.contains("Ngày mai", ignoreCase = true) -> Color(0xFFF97316)
                    badge.contains("Ngày kia", ignoreCase = true) -> Color(0xFF10B981)
                    badge.contains("ĐÃ UỐNG") -> Color(0xFF16A34A)
                    else -> Color(0xFF4F46E5)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = fg,
                    )
                }
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun AiAdvisorCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HeaderBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.carenest_logo_house),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI CỐ VẤN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HeaderBlue,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "\"$summary\"",
                fontSize = 14.sp,
                color = Color(0xFF1E293B),
                lineHeight = 22.sp,
            )
        }
    }
}

@Composable
private fun FamilyRow(
    family: Family,
    active: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) Color(0xFFEFF6FF) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = family.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) PrimaryBlue else Color(0xFF1E293B),
            )
            Text(
                text = "${family.memberCount} thành viên • ${family.role}",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
            )
        }
        if (active) {
            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
        }
    }
}

private fun buildDashboardTasks(
    tasks: List<DashboardTask>,
    medications: List<Medication>,
    appointments: List<Appointment>,
    vaccines: List<Vaccine>,
): List<DashboardTaskUi> {
    if (tasks.isNotEmpty()) {
        return tasks.map { task ->
            when (task.type?.uppercase()) {
                "MEDICATION" -> DashboardTaskUi(
                    icon = Icons.Default.MedicalServices,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = task.title,
                    subtitle = task.dueTime ?: task.description.orEmpty(),
                    badge = task.description,
                )

                "APPOINTMENT" -> DashboardTaskUi(
                    icon = Icons.Default.CalendarMonth,
                    iconBg = Color(0xFFF0FDF4),
                    iconTint = Color(0xFF16A34A),
                    title = task.title,
                    subtitle = task.dueTime ?: task.description.orEmpty(),
                    badge = task.description,
                )

                "VACCINATION" -> DashboardTaskUi(
                    icon = Icons.Default.Vaccines,
                    iconBg = Color(0xFFFFF7ED),
                    iconTint = Color(0xFFEA580C),
                    title = task.title,
                    subtitle = task.dueTime ?: task.description.orEmpty(),
                    badge = task.description,
                )

                else -> DashboardTaskUi(
                    icon = Icons.Default.CheckCircle,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = task.title,
                    subtitle = task.description.orEmpty(),
                )
            }
        }
    }

    val fallback = mutableListOf<DashboardTaskUi>()
    medications.firstOrNull()?.let {
        fallback += DashboardTaskUi(
            icon = Icons.Default.MedicalServices,
            iconBg = Color(0xFFEFF6FF),
            iconTint = Color(0xFF2563EB),
            title = it.name,
            subtitle = it.time,
            badge = if (it.isTaken) "ĐÃ UỐNG" else "CHƯA UỐNG",
        )
    }
    appointments.firstOrNull()?.let {
        fallback += DashboardTaskUi(
            icon = Icons.Default.CalendarMonth,
            iconBg = Color(0xFFF0FDF4),
            iconTint = Color(0xFF16A34A),
            title = if (it.doctorName.isBlank()) "Lịch hẹn khám" else it.doctorName,
            subtitle = it.date,
            badge = it.note,
        )
    }
    vaccines.firstOrNull()?.let {
        fallback += DashboardTaskUi(
            icon = Icons.Default.Vaccines,
            iconBg = Color(0xFFFFF7ED),
            iconTint = Color(0xFFEA580C),
            title = it.name,
            subtitle = it.date,
            badge = if (it.isCompleted) "ĐÃ TIÊM" else "CẦN THEO DÕI",
        )
    }
    return fallback
}

private fun selectedMemberText(selectedMemberId: String?, members: List<Member>): String {
    if (selectedMemberId == null) return "cả nhà"
    val member = members.firstOrNull { it.id == selectedMemberId } ?: return "thành viên"
    return member.name
        .trim()
        .split(" ")
        .lastOrNull()
        ?.lowercase(Locale.forLanguageTag("vi-VN"))
        ?: "thành viên"
}
