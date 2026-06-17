package com.example.carenest.feature.booking.presentation.doctorworkspace

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class DoctorBookingBadgeUi(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorWorkspaceScreen(
    onBack: () -> Unit,
    onNavigateToConsultationRoom: (Long) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: DoctorWorkspaceViewModel = viewModel(
        factory = DoctorWorkspaceViewModel.Factory(application.bookingRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmTarget by remember { mutableStateOf<BookingResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Phòng khám số", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadBookings() }) {
                            Text("Thử lại")
                        }
                    }
                }

                uiState.bookings.isEmpty() -> {
                    Text(
                        text = "Chưa có yêu cầu khám nào",
                        color = Color(0xFF64748B),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.bookings) { booking ->
                            BookingRequestCard(
                                booking = booking,
                                isBusy = uiState.busyBookingIds.contains(booking.id),
                                onApprove = {
                                    viewModel.approveBooking(
                                        id = booking.id,
                                        onSuccess = {
                                            scope.launch { snackbarHostState.showSnackbar("Đã chấp nhận yêu cầu") }
                                        },
                                        onError = { message ->
                                            scope.launch { snackbarHostState.showSnackbar(message) }
                                        }
                                    )
                                },
                                onReject = { reason ->
                                    viewModel.rejectBooking(
                                        id = booking.id,
                                        reason = reason,
                                        onSuccess = {
                                            scope.launch { snackbarHostState.showSnackbar("Đã từ chối yêu cầu") }
                                        },
                                        onError = { message ->
                                            scope.launch { snackbarHostState.showSnackbar(message) }
                                        }
                                    )
                                },
                                onConfirmSchedule = { confirmTarget = booking },
                                onNavigateToConsultationRoom = { onNavigateToConsultationRoom(booking.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    confirmTarget?.let { booking ->
        ConfirmScheduleDialog(
            booking = booking,
            onDismiss = { confirmTarget = null },
            onConfirm = { scheduledAtIso, confirmedLocation, confirmedNote ->
                viewModel.confirmSchedule(
                    id = booking.id,
                    scheduledAtIso = scheduledAtIso,
                    confirmedLocation = confirmedLocation,
                    confirmedNote = confirmedNote,
                    onSuccess = {
                        scope.launch { snackbarHostState.showSnackbar("Đã xác nhận lịch khám") }
                    },
                    onError = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )
                confirmTarget = null
            }
        )
    }
}

@Composable
private fun ConfirmScheduleDialog(
    booking: BookingResponse,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    val initialDateTime = remember(booking.scheduledAt) {
        booking.scheduledAt?.let {
            runCatching { Instant.parse(it).atZone(zoneId).toLocalDateTime() }.getOrNull()
        } ?: LocalDateTime.now().plusDays(1).withHour(9).withMinute(0)
    }
    var selectedDate by remember(booking.id) { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember(booking.id) { mutableStateOf(initialDateTime.toLocalTime().withSecond(0).withNano(0)) }
    var confirmedLocation by remember(booking.id) { mutableStateOf(booking.confirmedLocation.orEmpty()) }
    var confirmedNote by remember(booking.id) { mutableStateOf(booking.confirmedNote.orEmpty()) }
    val scheduledAtLocal = LocalDateTime.of(selectedDate, selectedTime)
    val isScheduledInPast = !scheduledAtLocal.isAfter(LocalDateTime.now())
    val requiresLocation = booking.requestType == BookingRequestType.OFFLINE_CLINIC
    val canConfirm = !isScheduledInPast && (!requiresLocation || confirmedLocation.isNotBlank())

    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).apply {
            datePicker.minDate = LocalDate.now().atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
    }
    val timePickerDialog = remember(selectedTime) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute -> selectedTime = LocalTime.of(hourOfDay, minute) },
            selectedTime.hour,
            selectedTime.minute,
            true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (booking.status == BookingStatus.APPROVED) {
                    "Cập nhật lịch đã xác nhận"
                } else {
                    "Xác nhận lịch cụ thể"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    onValueChange = {},
                    label = { Text("Ngày hẹn") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Chọn ngày")
                }

                OutlinedTextField(
                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = {},
                    label = { Text("Giờ hẹn") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Chọn giờ")
                }

                OutlinedTextField(
                    value = confirmedLocation,
                    onValueChange = { confirmedLocation = it },
                    label = {
                        Text(
                            if (booking.requestType == BookingRequestType.ONLINE_CHAT) {
                                "Nền tảng / địa điểm"
                            } else {
                                "Địa điểm / phòng khám"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            if (booking.requestType == BookingRequestType.ONLINE_CHAT) {
                                "Ví dụ: Google Meet hoặc số phòng khám nếu khám trực tiếp sau tư vấn"
                            } else {
                                "Ví dụ: Phòng khám Nhi, tầng 2"
                            }
                        )
                    },
                    isError = requiresLocation && confirmedLocation.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (requiresLocation && confirmedLocation.isBlank()) {
                    Text(
                        "Khám trực tiếp cần có địa điểm hoặc phòng khám.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = confirmedNote,
                    onValueChange = { confirmedNote = it },
                    label = { Text("Hướng dẫn thêm") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isScheduledInPast) {
                    Text(
                        "Thời gian hẹn phải nằm trong tương lai.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val scheduledAt = scheduledAtLocal.atZone(zoneId).toInstant().toString()
                    onConfirm(
                        scheduledAt,
                        confirmedLocation.takeIf { it.isNotBlank() },
                        confirmedNote.takeIf { it.isNotBlank() }
                    )
                },
                enabled = canConfirm
            ) {
                Text("Lưu lịch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun BookingRequestCard(
    booking: BookingResponse,
    isBusy: Boolean,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onConfirmSchedule: () -> Unit,
    onNavigateToConsultationRoom: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    if (showRejectDialog) {
        var rejectReason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Từ chối yêu cầu") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    placeholder = { Text("Nhập lý do từ chối...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            onReject(rejectReason)
                            showRejectDialog = false
                        }
                    },
                    enabled = rejectReason.isNotBlank() && !isBusy
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    val badge = doctorBookingBadgeUi(booking.status)
    val requestTypeLabel = if (booking.requestType == BookingRequestType.ONLINE_CHAT) {
        "Tư vấn trực tuyến"
    } else {
        "Khám trực tiếp"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (booking.patientAvatarUrl != null) {
                    AsyncImage(
                        model = booking.patientAvatarUrl,
                        contentDescription = "Avatar bệnh nhân",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = booking.patientFullName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.patientFullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = requestTypeLabel,
                        fontSize = 13.sp,
                        color = PrimaryBlue
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badge.backgroundColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        badge.label,
                        color = badge.textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Lý do khám:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
            Text(booking.note, fontSize = 14.sp, color = Color(0xFF0F172A))

            booking.healthProfileName?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Hồ sơ sức khỏe", it)
            }

            if (!booking.preferredTimeNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Thời gian mong muốn:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                Text(booking.preferredTimeNote, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            booking.scheduledAt?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Lịch đã xác nhận", compactIsoTime(it))
            }

            booking.confirmedLocation?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine(
                    if (booking.requestType == BookingRequestType.ONLINE_CHAT) "Nền tảng / địa điểm" else "Địa điểm",
                    it
                )
            }

            booking.confirmedNote?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Ghi chú bác sĩ", it)
            }

            if (booking.appointmentId != null && booking.status == BookingStatus.APPROVED) {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Đồng bộ lịch", "Đã ghi vào Lịch tái khám (#${booking.appointmentId})")
            }

            if (booking.status == BookingStatus.REJECTED && !booking.rejectReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lý do từ chối:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                Text(booking.rejectReason, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            if (booking.status == BookingStatus.CANCELLED && !booking.cancellationReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lý do hủy:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                Text(booking.cancellationReason, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            if (booking.status == BookingStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showRejectDialog = true }, enabled = !isBusy) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Từ chối", color = Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (booking.requestType == BookingRequestType.OFFLINE_CLINIC) {
                                onConfirmSchedule()
                            } else {
                                onApprove()
                            }
                        },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (booking.requestType == BookingRequestType.OFFLINE_CLINIC) "Xác nhận lịch" else "Chấp nhận")
                        }
                    }
                }
            } else if (booking.status == BookingStatus.APPROVED && booking.requestType == BookingRequestType.OFFLINE_CLINIC) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "Lịch khám trực tiếp đã được chốt. Bệnh nhân sẽ thấy lịch này trong Lịch tái khám.",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (booking.requestType == BookingRequestType.ONLINE_CHAT && booking.status.canOpenConsultationRoom()) {
                val buttonText = if (booking.status == BookingStatus.APPROVED || booking.status == BookingStatus.ACTIVE) {
                    "Vào phòng tư vấn riêng tư"
                } else {
                    "Xem lịch sử tư vấn"
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToConsultationRoom,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}

private fun doctorBookingBadgeUi(status: BookingStatus): DoctorBookingBadgeUi {
    return when (status) {
        BookingStatus.PENDING -> DoctorBookingBadgeUi("Chờ duyệt", Color(0xFFEAB308), Color(0xFFFEF9C3))
        BookingStatus.APPROVED -> DoctorBookingBadgeUi("Đã xác nhận", Color(0xFF22C55E), Color(0xFFDCFCE7))
        BookingStatus.REJECTED -> DoctorBookingBadgeUi("Đã từ chối", Color(0xFFEF4444), Color(0xFFFEE2E2))
        BookingStatus.ACTIVE -> DoctorBookingBadgeUi("Đang tư vấn", Color(0xFF3B82F6), Color(0xFFDBEAFE))
        BookingStatus.COMPLETED -> DoctorBookingBadgeUi("Hoàn tất", Color(0xFF64748B), Color(0xFFF1F5F9))
        BookingStatus.CANCELLED -> DoctorBookingBadgeUi("Đã hủy", Color(0xFFEF4444), Color(0xFFFEE2E2))
        BookingStatus.RESTRICTED -> DoctorBookingBadgeUi("Bị hạn chế", Color(0xFFF59E0B), Color(0xFFFEF3C7))
    }
}

private fun BookingStatus.canOpenConsultationRoom(): Boolean {
    return this == BookingStatus.APPROVED ||
        this == BookingStatus.ACTIVE ||
        this == BookingStatus.RESTRICTED ||
        this == BookingStatus.COMPLETED
}

@Composable
private fun BookingInfoLine(label: String, value: String) {
    Text("$label:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
    Text(value, fontSize = 14.sp, color = Color(0xFF0F172A))
}

private fun compactIsoTime(value: String): String {
    return value.replace("T", " ").replace("Z", "").take(16)
}
