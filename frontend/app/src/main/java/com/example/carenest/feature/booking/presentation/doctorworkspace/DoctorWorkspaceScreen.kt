package com.example.carenest.feature.booking.presentation.doctorworkspace

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
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingStatus

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về")
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
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
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
                                onApprove = {
                                    viewModel.approveBooking(
                                        id = booking.id,
                                        onSuccess = {},
                                        onError = { /* show toast */ }
                                    )
                                },
                                onReject = { reason ->
                                    viewModel.rejectBooking(
                                        id = booking.id,
                                        reason = reason,
                                        onSuccess = {},
                                        onError = { /* show toast */ }
                                    )
                                },
                                onNavigateToConsultationRoom = { onNavigateToConsultationRoom(booking.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingRequestCard(
    booking: BookingResponse,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
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
                    enabled = rejectReason.isNotBlank()
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
                
                // Status Badge
                val (statusColor, statusText, statusBg) = when(booking.status) {
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

            if (!booking.preferredTimeNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Thời gian mong muốn:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
                Text(booking.preferredTimeNote, fontSize = 14.sp, color = Color(0xFF0F172A))
            }
            
            if (booking.status == BookingStatus.REJECTED && !booking.rejectReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lý do từ chối:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFEF4444))
                Text(booking.rejectReason, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            if (booking.status == BookingStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showRejectDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Từ chối", color = Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chấp nhận")
                    }
                }
            } else if (booking.status == BookingStatus.APPROVED || booking.status == BookingStatus.ACTIVE) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToConsultationRoom,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Vào phòng tư vấn riêng tư")
                }
            }
        }
    }
}
