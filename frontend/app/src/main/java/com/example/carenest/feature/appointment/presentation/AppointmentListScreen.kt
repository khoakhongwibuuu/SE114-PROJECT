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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class FilterKey(val label: String) {
    ALL("Tất cả"),
    UPCOMING("Sắp tới"),
    PAST("Đã qua")
}

private data class AppointmentStatusUi(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color
)

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
        factory = AppointmentViewModelFactory(application.appointmentApi)
    )

    val state by vm.appointmentState.collectAsState()
    var selectedFilter by remember { mutableStateOf(FilterKey.ALL) }
    val resolvedProfileId = remember(profileId) { profileId.takeIf { it > 0L } }
    val hasProfile = resolvedProfileId != null && resolvedProfileId > 0L

    LaunchedEffect(resolvedProfileId) {
        if (hasProfile) {
            vm.fetchAppointments(resolvedProfileId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = PrimaryBlue
                        )
                    }
                    Text(
                        text = "Lịch tái khám",
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

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
                                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Chưa chọn hồ sơ sức khỏe",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            "Vui lòng chọn hoặc tạo hồ sơ trước khi xem lịch tái khám.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else when (val currentState = state) {
                is AppointmentState.Loading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                is AppointmentState.Error -> item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            currentState.message,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { vm.fetchAppointments(resolvedProfileId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }

                is AppointmentState.Empty -> item {
                    EmptyAppointmentState(
                        title = "Chưa có lịch tái khám",
                        subtitle = "Các lịch được bác sĩ xác nhận từ luồng đặt khám sẽ hiển thị tại đây. Bạn cũng có thể tự thêm lịch thủ công bằng nút cộng."
                    )
                }

            is AppointmentState.Success -> {
                    val filteredList = when (selectedFilter) {
                        FilterKey.ALL -> currentState.upcomingAppointments + currentState.appointmentHistory
                        FilterKey.UPCOMING -> currentState.upcomingAppointments
                        FilterKey.PAST -> currentState.appointmentHistory
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            EmptyAppointmentState(
                                title = "Không có lịch trong bộ lọc này",
                                subtitle = "Hãy đổi bộ lọc để xem lại các lịch khác."
                            )
                        }
                    } else {
                        items(filteredList, key = { "${it.javaClass.simpleName}_${it.id}" }) { item ->
                            val isUpcoming = item is AppointmentItem.Upcoming
                            val badge = appointmentStatusUi(item.status)
                            val timeText = formatAppointmentDate(item.appointmentDate)
                                ?: if (item is AppointmentItem.History) item.displayDate else ""
                            val location = when (item) {
                                is AppointmentItem.Upcoming -> item.location
                                is AppointmentItem.History -> item.location
                            }?.takeIf { it.isNotBlank() }

                            var showCompleteDialog by remember { mutableStateOf(false) }
                            var showCancelDialog by remember { mutableStateOf(false) }

                            if (showCompleteDialog) {
                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = { showCompleteDialog = false },
                                    title = { Text("Đánh dấu hoàn thành") },
                                    text = { Text("Bạn có chắc chắn muốn đánh dấu lịch khám này là đã hoàn thành không?") },
                                    confirmButton = {
                                        androidx.compose.material3.TextButton(onClick = {
                                            showCompleteDialog = false
                                            vm.completeAppointment(item.id, resolvedProfileId)
                                        }) {
                                            Text("Đồng ý", color = PrimaryBlue)
                                        }
                                    },
                                    dismissButton = {
                                        androidx.compose.material3.TextButton(onClick = { showCompleteDialog = false }) {
                                            Text("Hủy", color = Color.Gray)
                                        }
                                    }
                                )
                            }

                            if (showCancelDialog) {
                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = { showCancelDialog = false },
                                    title = { Text("Hủy lịch khám") },
                                    text = { Text("Bạn có chắc chắn muốn hủy bỏ lịch khám này không? Thao tác này không thể hoàn tác.") },
                                    confirmButton = {
                                        androidx.compose.material3.TextButton(onClick = {
                                            showCancelDialog = false
                                            vm.cancelAppointment(item.id, resolvedProfileId)
                                        }) {
                                            Text("Đồng ý", color = Color(0xFFC62828))
                                        }
                                    },
                                    dismissButton = {
                                        androidx.compose.material3.TextButton(onClick = { showCancelDialog = false }) {
                                            Text("Hủy", color = Color.Gray)
                                        }
                                    }
                                )
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .then(if (item.status == "CANCELLED") Modifier.alpha(0.72f) else Modifier),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(48.dp)
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isUpcoming) Color(0xFFCFE5FF) else Color(0xFFE2E8F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                val day = if (isUpcoming) {
                                                    (item as AppointmentItem.Upcoming).dayOfMonth
                                                } else {
                                                    "--"
                                                }
                                                val month = if (isUpcoming) {
                                                    val monthValue = parseAppointmentDate(item.appointmentDate)?.monthValue ?: 1
                                                    "Th${monthValue.toString().padStart(2, '0')}"
                                                } else {
                                                    "--"
                                                }

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

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = item.doctorName?.takeIf { it.isNotBlank() } ?: "Chưa cập nhật bác sĩ",
                                                fontSize = 13.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.AccessTime,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = Color(0xFF64748B)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = timeText.ifBlank { item.appointmentDate },
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            location?.let {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = Color(0xFF64748B)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = it,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(badge.backgroundColor)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = badge.label,
                                                color = badge.textColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (isUpcoming && item.status == "SCHEDULED") {
                                        androidx.compose.material3.Divider(color = Color(0xFFF1F5F9))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(
                                                onClick = { showCancelDialog = true },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Hủy lịch",
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            IconButton(
                                                onClick = { showCompleteDialog = true },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Hoàn thành",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
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
                    Toast.makeText(
                        context,
                        "Vui lòng chọn hoặc tạo hồ sơ sức khỏe trước.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 20.dp)
                .then(if (hasProfile) Modifier else Modifier.alpha(0.55f)),
            containerColor = PrimaryBlue,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm lịch hẹn")
        }
    }
}

@Composable
private fun EmptyAppointmentState(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = Color(0xFFCBD5E1)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
    }
}

private fun appointmentStatusUi(status: String): AppointmentStatusUi {
    return when (status.uppercase()) {
        "SCHEDULED" -> AppointmentStatusUi("Đã lên lịch", Color(0xFF1D4ED8), Color(0xFFDBEAFE))
        "COMPLETED" -> AppointmentStatusUi("Hoàn tất", Color(0xFF475569), Color(0xFFF1F5F9))
        "CANCELLED" -> AppointmentStatusUi("Đã hủy", Color(0xFFB91C1C), Color(0xFFFEE2E2))
        else -> AppointmentStatusUi(status, Color(0xFF334155), Color(0xFFE2E8F0))
    }
}

private fun parseAppointmentDate(value: String): ZonedDateTime? {
    return runCatching { ZonedDateTime.parse(value).withZoneSameInstant(ZoneId.systemDefault()) }.getOrNull()
        ?: runCatching { Instant.parse(value).atZone(ZoneId.systemDefault()) }.getOrNull()
}

private fun formatAppointmentDate(value: String): String? {
    val date = parseAppointmentDate(value) ?: return null
    val formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
    return date.format(formatter)
}
