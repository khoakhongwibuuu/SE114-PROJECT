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
import androidx.compose.material.icons.filled.ArrowBack
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
                title = { Text("PhÃ²ng khÃ¡m sá»‘", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trá»Ÿ vá»")
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
                            Text("Thá»­ láº¡i")
                        }
                    }
                }
                uiState.bookings.isEmpty() -> {
                    Text(
                        text = "ChÆ°a cÃ³ yÃªu cáº§u khÃ¡m nÃ o",
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
                                            scope.launch { snackbarHostState.showSnackbar("ÄÃ£ cháº¥p nháº­n yÃªu cáº§u") }
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
                                            scope.launch { snackbarHostState.showSnackbar("ÄÃ£ tá»« chá»‘i yÃªu cáº§u") }
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
                        scope.launch { snackbarHostState.showSnackbar("ÄÃ£ xÃ¡c nháº­n lá»‹ch khÃ¡m") }
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
        title = { Text("XÃ¡c nháº­n lá»‹ch cá»¥ thá»ƒ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    onValueChange = {},
                    label = { Text("NgÃ y háº¹n") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )
                OutlinedTextField(
                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onValueChange = {},
                    label = { Text("Giá» háº¹n") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                )
                OutlinedTextField(
                    value = confirmedLocation,
                    onValueChange = { confirmedLocation = it },
                    label = { Text("Äá»‹a Ä‘iá»ƒm / phÃ²ng khÃ¡m") },
                    isError = requiresLocation && confirmedLocation.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (requiresLocation && confirmedLocation.isBlank()) {
                    Text(
                        "KhÃ¡m trá»±c tiáº¿p cáº§n cÃ³ Ä‘á»‹a Ä‘iá»ƒm hoáº·c phÃ²ng khÃ¡m",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                OutlinedTextField(
                    value = confirmedNote,
                    onValueChange = { confirmedNote = it },
                    label = { Text("HÆ°á»›ng dáº«n thÃªm") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isScheduledInPast) {
                    Text(
                        "Thá»i gian háº¹n pháº£i á»Ÿ tÆ°Æ¡ng lai",
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
                Text("LÆ°u lá»‹ch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ÄÃ³ng")
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
            title = { Text("Tá»« chá»‘i yÃªu cáº§u") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    placeholder = { Text("Nháº­p lÃ½ do tá»« chá»‘i...") },
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
                    Text("XÃ¡c nháº­n")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Há»§y")
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
                        text = if (booking.requestType == BookingRequestType.ONLINE_CHAT) "TÆ° váº¥n trá»±c tuyáº¿n" else "KhÃ¡m trá»±c tiáº¿p",
                        fontSize = 13.sp,
                        color = PrimaryBlue
                    )
                }

                val (statusColor, statusText, statusBg) = when (booking.status) {
                    BookingStatus.PENDING -> Triple(Color(0xFFEAB308), "Chá» duyá»‡t", Color(0xFFFEF9C3))
                    BookingStatus.APPROVED -> Triple(Color(0xFF22C55E), "ÄÃ£ duyá»‡t", Color(0xFFDCFCE7))
                    BookingStatus.REJECTED -> Triple(Color(0xFFEF4444), "Tá»« chá»‘i", Color(0xFFFEE2E2))
                    BookingStatus.ACTIVE -> Triple(Color(0xFF3B82F6), "Äang khÃ¡m", Color(0xFFDBEAFE))
                    BookingStatus.COMPLETED -> Triple(Color(0xFF64748B), "HoÃ n thÃ nh", Color(0xFFF1F5F9))
                    BookingStatus.CANCELLED -> Triple(Color(0xFFEF4444), "ÄÃ£ há»§y", Color(0xFFFEE2E2))
                    BookingStatus.RESTRICTED -> Triple(Color(0xFFF59E0B), "Háº¡n cháº¿", Color(0xFFFEF3C7))
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

            Text("LÃ½ do khÃ¡m:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
            Text(booking.note, fontSize = 14.sp, color = Color(0xFF0F172A))

            booking.healthProfileName?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Há»“ sÆ¡ sá»©c khá»e", it)
            }

            if (!booking.preferredTimeNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Thá»i gian mong muá»‘n:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                Text(booking.preferredTimeNote, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            booking.scheduledAt?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Lá»‹ch Ä‘Ã£ xÃ¡c nháº­n", compactIsoTime(it))
            }

            booking.confirmedLocation?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Äá»‹a Ä‘iá»ƒm", it)
            }

            booking.confirmedNote?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Ghi chÃº bÃ¡c sÄ©", it)
            }

            if (booking.status == BookingStatus.REJECTED && !booking.rejectReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("LÃ½ do tá»« chá»‘i:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                Text(booking.rejectReason, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            if (booking.status == BookingStatus.CANCELLED && !booking.cancellationReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("LÃ½ do há»§y:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
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
                        Text("Tá»« chá»‘i", color = Color(0xFFEF4444))
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
                            Text(if (booking.requestType == BookingRequestType.OFFLINE_CLINIC) "XÃ¡c nháº­n lá»‹ch" else "Cháº¥p nháº­n")
                        }
                    }
                }
            } else if (booking.requestType == BookingRequestType.ONLINE_CHAT && booking.status.canOpenConsultationRoom()) {
                val buttonText = if (booking.status == BookingStatus.APPROVED || booking.status == BookingStatus.ACTIVE) {
                    "VÃ o phÃ²ng tÆ° váº¥n riÃªng tÆ°"
                } else {
                    "Xem lá»‹ch sá»­ tÆ° váº¥n"
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
