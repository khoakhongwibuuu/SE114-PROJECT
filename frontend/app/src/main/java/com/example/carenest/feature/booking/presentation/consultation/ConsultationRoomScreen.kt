package com.example.carenest.feature.booking.presentation.consultation

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.booking.domain.model.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationRoomScreen(
    bookingId: Long,
    viewModel: ConsultationRoomViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CareNestApplication
    val currentUserId = application.secureSessionManager.getUserId() ?: -1L

    val state by viewModel.state.collectAsState()
    var showDoctorMenu by remember { mutableStateOf(false) }
    var showEndSessionDialog by remember { mutableStateOf(false) }
    var showRestrictDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionSuccess) {
        state.actionSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearActionSuccess()
        }
    }

    LaunchedEffect(state.error, state.thread?.id) {
        val message = state.error
        if (message != null && state.thread != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(bookingId) {
        viewModel.loadRoom(bookingId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val thread = state.thread
                        if (thread != null) {
                            val isPatient = thread.patientId == currentUserId
                            val counterpartName = if (isPatient) {
                                "BS. ${thread.doctorFullName}"
                            } else {
                                thread.patientFullName
                            }
                            val counterpartAvatar = if (isPatient) {
                                thread.doctorAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${thread.doctorFullName}"
                            } else {
                                thread.patientAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${thread.patientFullName}"
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(counterpartName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Phiên tư vấn riêng tư", fontSize = 12.sp, color = Color.Gray)
                            }
                        } else {
                            Text("Phòng tư vấn riêng tư", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1E293B),
                    navigationIconContentColor = Color(0xFF1E293B)
                ),
                actions = {
                    val thread = state.thread
                    val controllableThread = thread?.takeIf {
                        it.doctorId == currentUserId &&
                            (it.status == BookingStatus.APPROVED ||
                                it.status == BookingStatus.ACTIVE ||
                                it.status == BookingStatus.RESTRICTED)
                    }
                    if (controllableThread != null) {
                        IconButton(
                            onClick = { showDoctorMenu = true },
                            enabled = !state.isActionLoading
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = Color(0xFF1E293B))
                        }
                        DropdownMenu(
                            expanded = showDoctorMenu,
                            onDismissRequest = { showDoctorMenu = false }
                        ) {
                            if (controllableThread.status != BookingStatus.RESTRICTED) {
                                DropdownMenuItem(
                                    text = { Text("Hạn chế nhắn tin", color = Color(0xFFF59E0B)) },
                                    onClick = {
                                        showDoctorMenu = false
                                        showRestrictDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Hủy hạn chế", color = Color(0xFF3B82F6)) },
                                    onClick = {
                                        showDoctorMenu = false
                                        val activeBookingId = state.thread?.bookingRequestId
                                        if (activeBookingId != null) {
                                            viewModel.unrestrictMessaging(activeBookingId)
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Kết thúc phiên", color = Color(0xFF22C55E)) },
                                onClick = {
                                    showDoctorMenu = false
                                    showEndSessionDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            val threadSnapshot = state.thread
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                state.error != null && threadSnapshot == null -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Không thể vào phòng tư vấn", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.error.orEmpty(), color = Color(0xFF64748B), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadRoom(bookingId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Thử lại", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                threadSnapshot != null -> {
                    val thread = threadSnapshot
                    if (!state.isConnected && !thread.status.isReadOnlyConsultation()) {
                        Surface(
                            color = Color(0xFFFFF7ED),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Đang mất kết nối phòng tư vấn. Tin nhắn mới có thể chưa gửi hoặc nhận được.",
                                color = Color(0xFF9A3412),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            reverseLayout = true
                        ) {
                            items(state.messages.size) { index ->
                                val msg = state.messages.reversed()[index]
                                val isMe = msg.senderId == currentUserId

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    if (!isMe) {
                                        AsyncImage(
                                            model = msg.senderAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${msg.senderName}",
                                            contentDescription = "Avatar",
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 260.dp)
                                            .background(
                                                color = if (isMe) PrimaryBlue else Color.White,
                                                shape = RoundedCornerShape(16.dp).copy(
                                                    bottomEnd = if (isMe) CornerSize(0.dp) else CornerSize(16.dp),
                                                    bottomStart = if (!isMe) CornerSize(0.dp) else CornerSize(16.dp)
                                                )
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = msg.content,
                                            color = if (isMe) Color.White else Color(0xFF1E293B),
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp, top = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info",
                                            tint = Color(0xFF3B82F6),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (thread.status == BookingStatus.COMPLETED) {
                                                    "Phiên tư vấn đã kết thúc."
                                                } else {
                                                    "Kết nối tư vấn thành công."
                                                },
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E3A8A),
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (thread.status == BookingStatus.COMPLETED) {
                                                    "Phiên chat này hiện chỉ ở chế độ đọc."
                                                } else {
                                                    "Bạn và bác sĩ đã có thể nhắn tin trực tiếp trong phòng chat này. Hãy giữ lịch sự và tôn trọng lẫn nhau."
                                                },
                                                color = Color(0xFF1E3A8A),
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var messageText by remember { mutableStateOf("") }
                        val isReadOnly = thread.status.isReadOnlyConsultation()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isReadOnly) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(24.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = when (thread.status) {
                                            BookingStatus.RESTRICTED -> "Nhắn tin đã bị hạn chế"
                                            BookingStatus.COMPLETED -> "Phiên tư vấn đã kết thúc"
                                            else -> "Không thể nhắn tin"
                                        },
                                        color = Color(0xFF94A3B8),
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFFE2E8F0), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send disabled",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Nhập tin nhắn...") },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color(0xFFF8FAFC)
                                    ),
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = {
                                        if (messageText.isNotBlank()) {
                                            val queued = viewModel.sendMessage(messageText)
                                            if (queued) {
                                                messageText = ""
                                            }
                                        }
                                    },
                                    enabled = messageText.isNotBlank() && state.isConnected,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (messageText.isNotBlank() && state.isConnected) PrimaryBlue else Color(0xFFE2E8F0),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (messageText.isNotBlank() && state.isConnected) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEndSessionDialog) {
        AlertDialog(
            onDismissRequest = { showEndSessionDialog = false },
            title = { Text("Kết thúc phiên tư vấn", fontWeight = FontWeight.Bold) },
            text = { Text("Phiên sẽ chuyển sang chế độ đọc và cả hai bên sẽ không gửi thêm tin nhắn được nữa. Lịch sử chat vẫn được lưu lại.") },
            confirmButton = {
                Button(
                    onClick = {
                        showEndSessionDialog = false
                        val activeBookingId = state.thread?.bookingRequestId
                        if (activeBookingId != null) {
                            viewModel.completeConsultation(activeBookingId) {}
                        }
                    },
                    enabled = !state.isActionLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) { Text("Kết thúc") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEndSessionDialog = false }) { Text("Hủy") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRestrictDialog) {
        AlertDialog(
            onDismissRequest = { showRestrictDialog = false },
            title = { Text("Hạn chế nhắn tin", fontWeight = FontWeight.Bold) },
            text = { Text("Bệnh nhân sẽ không gửi tin nhắn được nữa trong phiên này. Bạn vẫn có thể kết thúc phiên sau đó.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestrictDialog = false
                        val activeBookingId = state.thread?.bookingRequestId
                        if (activeBookingId != null) {
                            viewModel.restrictMessaging(activeBookingId)
                        }
                    },
                    enabled = !state.isActionLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) { Text("Hạn chế") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestrictDialog = false }) { Text("Hủy") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun BookingStatus?.isReadOnlyConsultation(): Boolean {
    return this == null ||
        this == BookingStatus.COMPLETED ||
        this == BookingStatus.REJECTED ||
        this == BookingStatus.CANCELLED ||
        this == BookingStatus.PENDING ||
        this == BookingStatus.RESTRICTED
}
