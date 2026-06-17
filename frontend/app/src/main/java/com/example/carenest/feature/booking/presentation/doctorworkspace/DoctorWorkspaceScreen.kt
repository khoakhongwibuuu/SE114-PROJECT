package com.example.carenest.feature.booking.presentation.doctorworkspace

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().plusHours(1).withMinute(0)) }
    var confirmedLocation by remember { mutableStateOf(booking.confirmedLocation.orEmpty()) }
    var confirmedNote by remember { mutableStateOf(booking.confirmedNote.orEmpty()) }
    val scheduledAtLocal = LocalDateTime.of(selectedDate, selectedTime)
    val isScheduledInPast = !scheduledAtLocal.isAfter(LocalDateTime.now())
    val requiresLocation = booking.requestType == BookingRequestType.OFFLINE_CLINIC
    val canConfirm = !isScheduledInPast && (!requiresLocation || confirmedLocation.isNotBlank())

    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> selectedDate = LocalDate.of(year, month + 1, dayOfMonth) },
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
        title = { Text("Xác nhận lịch cụ thể") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    onValueChange = {},
                    label = { Text("Ngày hẹn") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )
                OutlinedTextField(
                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = {},
                    label = { Text("Giờ hẹn") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                )
                OutlinedTextField(
                    value = confirmedLocation,
                    onValueChange = { confirmedLocation = it },
                    label = { Text("Địa điểm / phòng khám") },
                    isError = requiresLocation && confirmedLocation.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (requiresLocation && confirmedLocation.isBlank()) {
                    Text(
                        "Khám trực tiếp cần có địa điểm hoặc phòng khám",
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
                        "Thời gian hẹn phải ở tương lai",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val scheduledAt = scheduledAtLocal.atZone(zoneId)
                        .toInstant()
                        .toString()
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
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
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
                        text = if (booking.requestType == BookingRequestType.ONLINE_CHAT) "Tư vấn trực tuyến" else "Khám trực tiếp",
                        fontSize = 13.sp,
                        color = PrimaryBlue
                    )
                }

                val (statusColor, statusText, statusBg) = when (booking.status) {
                    BookingStatus.PENDING -> Triple(Color(0xFFEAB308), "Chờ duyệt", Color(0xFFFEF9C3))
                    BookingStatus.APPROVED -> Triple(Color(0xFF22C55E), "Đã duyệt", Color(0xFFDCFCE7))
                    BookingStatus.REJECTED -> Triple(Color(0xFFEF4444), "Từ chối", Color(0xFFFEE2E2))
                    BookingStatus.ACTIVE -> Triple(Color(0xFF3B82F6), "Đang khám", Color(0xFFDBEAFE))
                    BookingStatus.COMPLETED -> Triple(Color(0xFF64748B), "Hoàn thành", Color(0xFFF1F5F9))
                    BookingStatus.CANCELLED -> Triple(Color(0xFFEF4444), "Đã hủy", Color(0xFFFEE2E2))
                    BookingStatus.RESTRICTED -> Triple(Color(0xFFF59E0B), "Hạn chế", Color(0xFFFEF3C7))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
                BookingInfoLine("Địa điểm", it)
            }

            booking.confirmedNote?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Ghi chú bác sĩ", it)
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
                    TextButton(
                        onClick = { showRejectDialog = true },
                        enabled = !isBusy
                    ) {
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
