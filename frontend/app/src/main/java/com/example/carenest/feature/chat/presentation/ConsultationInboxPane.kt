package com.example.carenest.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.booking.domain.model.BookingStatus
import com.example.carenest.feature.booking.domain.model.ConsultationThreadInboxResponse

@Composable
fun ConsultationInboxPane(
    onNavigateToConsultationRoom: (Long) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val currentUserId = application.secureSessionManager.getUserId() ?: -1L
    val viewModel: ConsultationInboxViewModel = viewModel(
        factory = ConsultationInboxViewModel.Factory(application.bookingRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInbox()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Không thể tải danh sách tư vấn",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error.orEmpty(),
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::refresh,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thử lại", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            uiState.threads.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chưa có cuộc tư vấn nào",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Các cuộc tư vấn riêng tư được chấp nhận sẽ hiển thị tại đây.",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                val active = uiState.threads.filter { it.status == BookingStatus.ACTIVE || it.status == BookingStatus.APPROVED }
                val restricted = uiState.threads.filter { it.status == BookingStatus.RESTRICTED }
                val completed = uiState.threads.filter { it.status == BookingStatus.COMPLETED }
                val others = uiState.threads.filterNot { thread ->
                    thread in active || thread in restricted || thread in completed
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (active.isNotEmpty()) {
                        item {
                            SectionHeader(label = "Đang tư vấn", color = Color(0xFF22C55E))
                        }
                        items(active, key = { it.id }) { thread ->
                            ConsultationThreadCard(
                                thread = thread,
                                currentUserId = currentUserId,
                                onClick = { onNavigateToConsultationRoom(thread.latestBookingId) }
                            )
                        }
                    }

                    if (restricted.isNotEmpty()) {
                        item {
                            SectionHeader(label = "Đã hạn chế nhắn tin", color = Color(0xFFF59E0B))
                        }
                        items(restricted, key = { it.id }) { thread ->
                            ConsultationThreadCard(
                                thread = thread,
                                currentUserId = currentUserId,
                                onClick = { onNavigateToConsultationRoom(thread.latestBookingId) }
                            )
                        }
                    }

                    if (completed.isNotEmpty()) {
                        item {
                            SectionHeader(label = "Đã kết thúc", color = Color(0xFF64748B))
                        }
                        items(completed, key = { it.id }) { thread ->
                            ConsultationThreadCard(
                                thread = thread,
                                currentUserId = currentUserId,
                                onClick = { onNavigateToConsultationRoom(thread.latestBookingId) }
                            )
                        }
                    }

                    if (others.isNotEmpty()) {
                        item {
                            SectionHeader(label = "Khác", color = Color(0xFF64748B))
                        }
                        items(others, key = { it.id }) { thread ->
                            ConsultationThreadCard(
                                thread = thread,
                                currentUserId = currentUserId,
                                onClick = { onNavigateToConsultationRoom(thread.latestBookingId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(label: String, color: Color) {
    Text(
        text = label,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun ConsultationThreadCard(
    thread: ConsultationThreadInboxResponse,
    currentUserId: Long,
    onClick: () -> Unit
) {
    val isPatient = thread.patientId == currentUserId
    val counterpartName = if (isPatient) {
        thread.doctorFullName.ifBlank { "Bác sĩ CareNest" }.let { "BS. $it" }
    } else {
        thread.patientFullName.ifBlank { "Bệnh nhân CareNest" }
    }

    val counterpartSeed = if (isPatient) {
        thread.doctorFullName.ifBlank { "doctor-${thread.doctorId}" }
    } else {
        thread.patientFullName.ifBlank { "patient-${thread.patientId}" }
    }
    val counterpartAvatar = if (isPatient) {
        thread.doctorAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=$counterpartSeed"
    } else {
        thread.patientAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=$counterpartSeed"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = counterpartAvatar,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = counterpartName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    val (statusColor, statusText) = thread.status.toInboxStatus()
                    if (statusText.isNotBlank()) {
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (thread.status) {
                        BookingStatus.COMPLETED -> "Phiên tư vấn đã lưu trữ"
                        BookingStatus.RESTRICTED -> "Nhắn tin đã bị hạn chế"
                        BookingStatus.ACTIVE -> "Chạm để tiếp tục cuộc tư vấn"
                        BookingStatus.APPROVED -> "Chạm để mở phòng tư vấn"
                        else -> "Chạm để xem chi tiết cuộc tư vấn"
                    },
                    color = if (thread.status == BookingStatus.RESTRICTED) Color(0xFFF59E0B) else Color(0xFF64748B),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun BookingStatus.toInboxStatus(): Pair<Color, String> {
    return when (this) {
        BookingStatus.APPROVED -> Color(0xFF22C55E) to "Đã duyệt"
        BookingStatus.ACTIVE -> Color(0xFF3B82F6) to "Đang tư vấn"
        BookingStatus.COMPLETED -> Color(0xFF64748B) to "Đã kết thúc"
        BookingStatus.RESTRICTED -> Color(0xFFF59E0B) to "Hạn chế"
        BookingStatus.CANCELLED -> Color(0xFFDC2626) to "Đã hủy"
        BookingStatus.REJECTED -> Color(0xFFDC2626) to "Đã từ chối"
        BookingStatus.PENDING -> Color(0xFF64748B) to "Chờ xử lý"
        else -> Color(0xFF64748B) to ""
    }
}
