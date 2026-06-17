package com.example.carenest.feature.booking.presentation.patient

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.carenest.feature.booking.domain.model.BookingRequestType
import com.example.carenest.feature.booking.domain.model.BookingResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus

private data class BookingBadgeUi(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientBookingCenterScreen(
    onBack: () -> Unit,
    onNavigateToConsultationRoom: (Long) -> Unit,
    onNavigateToAppointments: (Long) -> Unit
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
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
                        onFindDoctor = onBack
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
                                onNavigateToConsultationRoom = {
                                    onNavigateToConsultationRoom(booking.id)
                                },
                                onNavigateToAppointments = { profileId ->
                                    onNavigateToAppointments(profileId)
                                }
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
    onFindDoctor: () -> Unit
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
            text = "Bạn có thể đặt lịch từ hồ sơ bác sĩ. Khi có yêu cầu, trạng thái duyệt, lịch hẹn xác nhận và phòng tư vấn sẽ hiển thị tại đây.",
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onFindDoctor,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Quay lại")
        }
    }
}

@Composable
fun PatientBookingCard(
    booking: BookingResponse,
    onNavigateToConsultationRoom: () -> Unit,
    onNavigateToAppointments: (Long) -> Unit
) {
    val badge = bookingBadgeUi(booking.status)
    val doctorDisplayName = booking.doctorFullName
        ?.takeIf { it.isNotBlank() }
        ?.let { "BS. $it" }
        ?: "Bác sĩ đang cập nhật"
    val typeLabel = if (booking.requestType == BookingRequestType.ONLINE_CHAT) {
        "Tư vấn trực tuyến"
    } else {
        "Khám trực tiếp"
    }
    val targetProfileId = booking.healthProfileId?.takeIf { it > 0L }
    val hasAppointmentLedger = booking.appointmentId != null && targetProfileId != null

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
                        model = booking.doctorAvatarUrl
                            ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${booking.doctorFullName ?: booking.doctorId}",
                        contentDescription = "Avatar bác sĩ",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doctorDisplayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = typeLabel,
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

            Text(
                "Ghi chú của bạn:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF334155)
            )
            Text(booking.note, fontSize = 14.sp, color = Color(0xFF0F172A))

            booking.healthProfileName?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Hồ sơ sức khỏe", it)
            }

            if (!booking.preferredTimeNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Thời gian mong muốn:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )
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
                BookingInfoLine("Hướng dẫn của bác sĩ", it)
            }

            if (hasAppointmentLedger) {
                Spacer(modifier = Modifier.height(8.dp))
                BookingInfoLine("Đồng bộ lịch", "Đã ghi vào Lịch tái khám (#${booking.appointmentId})")
            }

            if (booking.status == BookingStatus.REJECTED && !booking.rejectReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Lý do từ chối:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFFEF4444)
                )
                Text(booking.rejectReason, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            if (booking.status == BookingStatus.CANCELLED && !booking.cancellationReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Lý do hủy:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFFEF4444)
                )
                Text(booking.cancellationReason, fontSize = 14.sp, color = Color(0xFF0F172A))
            }

            if (booking.requestType == BookingRequestType.ONLINE_CHAT && booking.status.canOpenConsultationRoom()) {
                val buttonText = if (
                    booking.status == BookingStatus.APPROVED ||
                    booking.status == BookingStatus.ACTIVE
                ) {
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

            if (hasAppointmentLedger) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onNavigateToAppointments(targetProfileId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mở lịch tái khám")
                }
            }
        }
    }
}

private fun bookingBadgeUi(status: BookingStatus): BookingBadgeUi {
    return when (status) {
        BookingStatus.PENDING -> BookingBadgeUi("Chờ duyệt", Color(0xFFEAB308), Color(0xFFFEF9C3))
        BookingStatus.APPROVED -> BookingBadgeUi("Đã xác nhận", Color(0xFF22C55E), Color(0xFFDCFCE7))
        BookingStatus.REJECTED -> BookingBadgeUi("Đã từ chối", Color(0xFFEF4444), Color(0xFFFEE2E2))
        BookingStatus.ACTIVE -> BookingBadgeUi("Đang tư vấn", Color(0xFF3B82F6), Color(0xFFDBEAFE))
        BookingStatus.COMPLETED -> BookingBadgeUi("Hoàn tất", Color(0xFF64748B), Color(0xFFF1F5F9))
        BookingStatus.CANCELLED -> BookingBadgeUi("Đã hủy", Color(0xFFEF4444), Color(0xFFFEE2E2))
        BookingStatus.RESTRICTED -> BookingBadgeUi("Bị hạn chế", Color(0xFFF59E0B), Color(0xFFFEF3C7))
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
