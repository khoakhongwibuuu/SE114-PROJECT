package com.example.carenest.feature.appointment.presentation

import android.widget.Toast

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue

private enum class FilterKey(val label: String) {
    ALL("Tất cả"),
    UPCOMING("Sắp tới"),
    PAST("Đã qua")
}

@Composable
fun AppointmentListScreen(
    onBack: () -> Unit,
    onAddAppointment: () -> Unit = {},
    profileId: Long = 0L,
    viewModel: AppointmentViewModel? = null
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication

    val vm: AppointmentViewModel = viewModel ?: viewModel(
        factory = AppointmentViewModelFactory(application.appointmentApi, application.secureSessionManager)
    )

    val state by vm.appointmentState.collectAsState()
    var selectedFilter by remember { mutableStateOf(FilterKey.ALL) }
    val resolvedProfileId = remember(profileId) {
        profileId.takeIf { it > 0L }
    }
    val hasProfile = resolvedProfileId != null && resolvedProfileId > 0L

    LaunchedEffect(resolvedProfileId) {
        if (hasProfile) {
            vm.fetchAppointments(resolvedProfileId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = PrimaryBlue)
                    }
                    Text(text = "Lịch tái khám", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Filters
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(FilterKey.entries, key = { it.name }) { filter ->
                        val selected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) PrimaryBlue else Color(0xFFE2E8F0))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = filter.label,
                                color = if (selected) Color.White else Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (!hasProfile) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chưa chọn hồ sơ sức khỏe", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Vui lòng chọn hoặc tạo hồ sơ trước khi xem lịch tái khám.", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
            } else when (val currentState = state) {
                is AppointmentState.Loading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is AppointmentState.Error -> item {
                    Text("⚠️ ${currentState.message}", color = Color(0xFFC62828), modifier = Modifier.padding(16.dp))
                }
                is AppointmentState.Empty -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Không có lịch nào", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Nhấn + để thêm lịch tái khám mới", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
                is AppointmentState.Success -> {
                    val filteredList = when (selectedFilter) {
                        FilterKey.ALL -> currentState.upcomingAppointments + currentState.appointmentHistory
                        FilterKey.UPCOMING -> currentState.upcomingAppointments
                        FilterKey.PAST -> currentState.appointmentHistory
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color(0xFFCBD5E1))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Không có lịch nào", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }
                    } else {
                        items(filteredList, key = { "${it.javaClass.simpleName}_${it.id}" }) { item ->
                            val isUpcoming = item is AppointmentItem.Upcoming
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .then(if (!isUpcoming) Modifier.alpha(0.7f) else Modifier),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Date Pill
                                    Box(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isUpcoming) Color(0xFFCFE5FF) else Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val day = if (isUpcoming) item.dayOfMonth else "--"
                                            val month = if (isUpcoming) {
                                                val m = try {
                                                    java.time.ZonedDateTime.parse(item.appointmentDate).monthValue
                                                } catch (e: Exception) { 1 }
                                                "Th${m.toString().padStart(2, '0')}"
                                            } else "--"

                                            Text(
                                                text = day,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isUpcoming) PrimaryBlue else Color(0xFF64748B)
                                            )
                                            Text(
                                                text = month,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isUpcoming) PrimaryBlue else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Content
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                        Text(text = item.doctorName ?: "Chưa rõ", fontSize = 13.sp, color = Color(0xFF64748B))
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF64748B))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val timeStr = if (isUpcoming) {
                                                try {
                                                    val zdt = java.time.ZonedDateTime.parse(item.appointmentDate)
                                                    zdt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))
                                                } catch (e: Exception) { "" }
                                            } else {
                                                (item as AppointmentItem.History).displayDate
                                            }
                                            Text(text = timeStr, fontSize = 12.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    // Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isUpcoming) Color(0xFFE8DEF8) else Color(0xFFE2E8F0))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isUpcoming) "Sắp tới" else "Đã qua",
                                            color = if (isUpcoming) Color(0xFF6750A4) else Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
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
            onClick = {
                if (hasProfile) {
                    onAddAppointment()
                } else {
                    Toast.makeText(context, "Vui lòng chọn hoặc tạo hồ sơ sức khỏe trước", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp)
                .then(if (hasProfile) Modifier else Modifier.alpha(0.55f)),
            containerColor = PrimaryBlue,
            contentColor = Color.White,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm lịch hẹn")
        }
    }
}
