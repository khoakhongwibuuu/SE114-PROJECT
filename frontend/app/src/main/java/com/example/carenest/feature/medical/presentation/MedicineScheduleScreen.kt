package com.example.carenest.feature.medical.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.medical.data.remote.MedicationLogResponse

private data class SessionConfig(
    val icon: ImageVector,
    val iconContainer: Color,
    val iconTint: Color,
)

private val SESSION_CONFIGS = mapOf(
    "MORNING" to SessionConfig(Icons.Default.LightMode, Color(0xFFFFF9C4), Color(0xFFF9A825)),
    "NOON" to SessionConfig(Icons.Default.WbTwilight, Color(0xFFE3F2FD), Color(0xFF1976D2)),
    "EVENING" to SessionConfig(Icons.Default.Bedtime, Color(0xFFEDE7F6), Color(0xFF7B1FA2)),
    "UNSCHEDULED" to SessionConfig(Icons.Default.AccessTime, Color(0xFFF1F5F9), Color(0xFF64748B)),
)

private val DEFAULT_SESSION_CONFIG = SessionConfig(
    Icons.Default.AccessTime,
    Color(0xFFF1F5F9),
    Color(0xFF64748B),
)

private val SESSION_LABELS = mapOf(
    "MORNING" to "Buổi sáng",
    "NOON" to "Buổi trưa",
    "EVENING" to "Buổi tối",
    "UNSCHEDULED" to "Chưa xác định",
)

@Composable
fun MedicineScheduleScreen(
    onBack: () -> Unit,
    onAddSchedule: () -> Unit = {},
    profileId: Long = 0L,
    viewModel: MedicineViewModel,
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication

    val scheduleState by viewModel.scheduleState.collectAsState()
    val resolvedProfileId = remember(profileId) {
        profileId.takeIf { it > 0L }
            ?: application.secureSessionManager.getActiveProfileId()
            ?: application.secureSessionManager.getProfileId()
    }

    LaunchedEffect(resolvedProfileId) {
        resolvedProfileId?.takeIf { it > 0 }?.let { viewModel.fetchTodaySchedule(it) }
    }

    val hasActiveProfile = resolvedProfileId != null && resolvedProfileId > 0L

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        if (!hasActiveProfile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bạn chưa có hồ sơ y tế cá nhân. Vui lòng cập nhật hồ sơ để sử dụng tính năng này.",
                    fontSize = 16.sp,
                    color = Color(0xFF475569),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Quay lại", color = Color.White)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
                    }
                    Text(text = "Lịch uống thuốc", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            when (val state = scheduleState) {
                is ScheduleState.Loading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                is ScheduleState.Error -> item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7F7)),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = state.message,
                                color = Color(0xFFC62828),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            resolvedProfileId?.let { safeProfileId ->
                                Button(
                                    onClick = { viewModel.fetchTodaySchedule(safeProfileId) },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                ) {
                                    Text("Thử lại", color = Color.White)
                                }
                            }
                        }
                    }
                }

                is ScheduleState.Empty -> item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Chưa có lịch thuốc nào cho hồ sơ đang chọn.",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp,
                            )
                            Button(
                                onClick = onAddSchedule,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            ) {
                                Text("Tạo lịch đầu tiên", color = Color.White)
                            }
                        }
                    }
                }

                is ScheduleState.Success -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(PrimaryBlue)
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(state.profileName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${state.takenCount}/${state.totalCount} lần uống đã hoàn thành",
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 13.sp,
                                )
                            }
                            val progress =
                                if (state.totalCount == 0) 0 else ((state.takenCount * 100f) / state.totalCount).toInt()
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("$progress%", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    state.sections.forEach { section ->
                        val config = SESSION_CONFIGS[section.session] ?: DEFAULT_SESSION_CONFIG
                        val label = SESSION_LABELS[section.session] ?: section.session

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(config.iconContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(config.icon, contentDescription = null, tint = config.iconTint, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                            ) {
                                Column {
                                    section.items.forEachIndexed { index, log ->
                                        ScheduleRow(
                                            log = log,
                                            showDivider = index < section.items.lastIndex,
                                            onToggle = { viewModel.toggleDose(log.id, log.status == "TAKEN") },
                                            onDelete = {
                                                log.medicationId?.let { medId ->
                                                    viewModel.deleteSchedule(medId, resolvedProfileId)
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }

        FloatingActionButton(
            onClick = onAddSchedule,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm lịch")
        }
    }
    }
}

@Composable
private fun ScheduleRow(
    log: MedicationLogResponse,
    showDivider: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val taken = log.status == "TAKEN"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (taken) PrimaryBlue else Color.Transparent)
                .then(if (!taken) Modifier.border(2.dp, Color(0xFFBFC7D3), CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            if (taken) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.medicineName,
                color = if (taken) Color(0xFF94A3B8) else Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            val detail = buildString {
                append(log.dosage)
                log.notes?.let { append(" - $it") }
            }
            Text(text = detail, color = Color(0xFF64748B), fontSize = 12.sp)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFDC2626))
        }
    }

    if (showDivider) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))
    }
}
