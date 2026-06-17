package com.example.carenest.feature.chat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.booking.domain.model.ConsultationThreadInboxResponse
import com.example.carenest.feature.booking.domain.model.BookingStatus

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
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(uiState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadInbox() }) {
                        Text("Thử lại")
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            else -> {
                val active = uiState.threads.filter { it.status == BookingStatus.ACTIVE || it.status == BookingStatus.APPROVED }
                val restricted = uiState.threads.filter { it.status == BookingStatus.RESTRICTED }
                val completed = uiState.threads.filter { it.status == BookingStatus.COMPLETED }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (active.isNotEmpty()) {
                        item {
                            SectionHeader(
                                label = "🟢  Đang tư vấn",
                                color = Color(0xFF22C55E)
                            )
                        }
                        items(active) { thread ->
                            ConsultationThreadCard(thread = thread, currentUserId = currentUserId, onClick = { onNavigateToConsultationRoom(thread.latestBookingId) })
                        }
                    }

                    if (restricted.isNotEmpty()) {
                        item {
                            SectionHeader(
                                label = "🚫  Hạn chế nhắn tin",
                                color = Color(0xFFF59E0B)
                            )
                        }
                        items(restricted) { thread ->
                            ConsultationThreadCard(thread = thread, currentUserId = currentUserId, onClick = { onNavigateToConsultationRoom(thread.latestBookingId) })
                        }
                    }

                    if (completed.isNotEmpty()) {
                        item {
                            SectionHeader(
                                label = "⏹️  Đã kết thúc",
                                color = Color(0xFF64748B)
                            )
                        }
                        items(completed) { thread ->
                            ConsultationThreadCard(thread = thread, currentUserId = currentUserId, onClick = { onNavigateToConsultationRoom(thread.latestBookingId) })
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
        "BS. ${thread.doctorFullName ?: thread.doctorId}"
    } else {
        thread.patientFullName
    }
    
    val counterpartAvatar = if (isPatient) {
        thread.doctorAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${thread.doctorFullName ?: thread.doctorId}"
    } else {
        thread.patientAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${thread.patientFullName}"
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

                    // Status indicator
                    val (statusColor, statusText) = when(thread.status) {
                        BookingStatus.APPROVED -> Pair(Color(0xFF22C55E), "Đã duyệt")
                        BookingStatus.ACTIVE -> Pair(Color(0xFF3B82F6), "Đang khám")
                        BookingStatus.COMPLETED -> Pair(Color(0xFF64748B), "Đã kết thúc")
                        BookingStatus.RESTRICTED -> Pair(Color(0xFFF59E0B), "Hạn chế")
                        else -> Pair(Color.Gray, "")
                    }
                    
                    if (statusText.isNotEmpty()) {
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
                        BookingStatus.RESTRICTED -> "⛔ Nhắn tin đã bị hạn chế"
                        else -> "Chạm để mở phiên tư vấn"
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
