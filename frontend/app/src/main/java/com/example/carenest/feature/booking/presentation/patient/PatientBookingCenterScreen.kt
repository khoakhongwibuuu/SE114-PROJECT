package com.example.carenest.feature.booking.presentation.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientBookingCenterScreen(
    onBack: () -> Unit,
    onNavigateToConsultationRoom: (Long) -> Unit,
    onNavigateToDoctorProfile: (Long) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val viewModel: PatientBookingCenterViewModel = viewModel(
        factory = PatientBookingCenterViewModel.Factory(application.bookingRepository)
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
                title = { Text("Lịch sử đặt khám", fontWeight = FontWeight.Bold) },
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
                    EmptyBookingHistoryState(
                        modifier = Modifier.align(Alignment.Center),
                        doctors = uiState.doctors,
                        onFindDoctor = { doctorId ->
                            if (doctorId != null) {
                                onNavigateToDoctorProfile(doctorId)
                            } else {
                                onBack()
                            }
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.bookings) { booking ->
                            PatientBookingCard(
                                booking = booking,
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
private fun EmptyBookingHistoryState(
    modifier: Modifier = Modifier,
    doctors: List<DoctorSummary>,
    onFindDoctor: (Long?) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Chưa có lịch sử đặt khám",
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bạn có thể đặt lịch từ hồ sơ bác sĩ. Khi có yêu cầu, trạng thái và phòng tư vấn sẽ hiển thị tại đây.",
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (doctors.isNotEmpty()) {
            Text(
                text = "Bác sĩ đã xác thực",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(doctors.take(5), key = { it.id }) { doctor ->
                    RecommendedDoctorCard(
                        doctor = doctor,
                        onClick = { onFindDoctor(doctor.id) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { onFindDoctor(null) }) {
                Text("Quay lại")
            }
        } else {
            Button(
                onClick = { onFindDoctor(null) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Quay lại")
            }
        }
    }
}

@Composable
private fun RecommendedDoctorCard(
    doctor: DoctorSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = doctor.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A),
                maxLines = 1
            )
            Text(
                text = doctor.specialty ?: "Chưa cập nhật chuyên khoa",
                fontSize = 13.sp,
                color = PrimaryBlue,
                maxLines = 1
            )
            Text(
                text = doctor.hospitalName ?: "Chưa cập nhật cơ sở",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                maxLines = 2
            )
            Text(
                text = "Mở hồ sơ bác sĩ",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2563EB)
            )
        }
    }
}

@Composable
fun PatientBookingCard(
    booking: BookingResponse,
    onNavigateToConsultationRoom: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = booking.doctorAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${booking.doctorFullName ?: booking.doctorId}",
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BS. ${booking.doctorFullName ?: booking.doctorId}",
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
                    BookingStatus.APPROVED -> Triple(Color(0xFF22C55E), "Đã chấp nhận", Color(0xFFDCFCE7))
                    BookingStatus.REJECTED -> Triple(Color(0xFFEF4444), "Đã từ chối", Color(0xFFFEE2E2))
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

            Text("Ghi chú của bạn:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF334155))
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
                BookingInfoLine("Hướng dẫn của bác sĩ", it)
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

            if (booking.requestType == BookingRequestType.ONLINE_CHAT && booking.status.canOpenConsultationRoom()) {
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
