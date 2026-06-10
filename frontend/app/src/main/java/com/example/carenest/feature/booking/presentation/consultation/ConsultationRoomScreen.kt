package com.example.carenest.feature.booking.presentation.consultation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess != null) {
            snackbarHostState.showSnackbar(state.actionSuccess!!)
            viewModel.clearActionSuccess()
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
                        if (state.thread != null) {
                            val thread = state.thread!!
                            
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
                    val isDoctor = thread != null && thread.doctorId == currentUserId
                    val canControl = isDoctor &&
                        thread?.status != BookingStatus.COMPLETED &&
                        thread?.status != BookingStatus.REJECTED
                    if (canControl) {
                        IconButton(onClick = { showDoctorMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = Color(0xFF1E293B))
                        }
                        DropdownMenu(
                            expanded = showDoctorMenu,
                            onDismissRequest = { showDoctorMenu = false }
                        ) {
                            if (thread?.status != BookingStatus.RESTRICTED) {
                                DropdownMenuItem(
                                    text = { Text("ⓘ  Hạn chế nhắn tin", color = Color(0xFFF59E0B)) },
                                    onClick = {
                                        showDoctorMenu = false
                                        showRestrictDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("ⓘ  Hủy hạn chế", color = Color(0xFF3B82F6)) },
                                    onClick = {
                                        showDoctorMenu = false
                                        val bookingId = state.thread?.bookingRequestId
                                        if (bookingId != null) {
                                            viewModel.unrestrictMessaging(bookingId)
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("✓  Kết thúc phiên", color = Color(0xFF22C55E)) },
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
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                state.error != null -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Lỗi: ${state.error}", color = Color.Red)
                    }
                }
                state.thread != null -> {
                    val thread = state.thread!!
                       // Chat message list area
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            reverseLayout = true
                        ) {
                            // Messages (reversed because LazyColumn has reverseLayout=true)
                            items(state.messages.size) { index ->
                                // Note: with reverseLayout = true, item 0 is at bottom.
                                // We reverse the list for displaying bottom-up.
                                val msg = state.messages.reversed()[index]
                                val isMe = msg.senderId == currentUserId
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    if (!isMe) {
                                        AsyncImage(
                                            model = msg.senderAvatarUrl ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${msg.senderName}",
                                            contentDescription = "Avatar",
                                            modifier = Modifier.size(32.dp).clip(CircleShape),
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
                                // Honest UX Alert
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp)
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
                                                text = if (thread.status == BookingStatus.COMPLETED) "Phiên tư vấn đã kết thúc." else "Kết nối tư vấn thành công.",
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E3A8A),
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (thread.status == BookingStatus.COMPLETED) "Phiên chat này hiện chỉ ở chế độ đọc." else "Bạn và bác sĩ đã có thể nhắn tin trực tiếp trong phòng chat này. Hãy giữ lịch sự và tôn trọng lẫn nhau.",
                                                color = Color(0xFF1E3A8A),
                                                fontSize = 13.sp,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }                      // Input Bar
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var messageText by remember { mutableStateOf("") }
                    val isReadOnly = thread.status == BookingStatus.COMPLETED
                        || thread.status == BookingStatus.REJECTED
                        || thread.status == BookingStatus.PENDING
                        || thread.status == BookingStatus.RESTRICTED

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
                                            BookingStatus.RESTRICTED -> "⛔ Nhắn tin đã bị hạn chế"
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
                                            viewModel.sendMessage(messageText)
                                            messageText = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(if (messageText.isNotBlank()) PrimaryBlue else Color(0xFFE2E8F0), shape = CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (messageText.isNotBlank()) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // End session dialog
    if (showEndSessionDialog) {
        AlertDialog(
            onDismissRequest = { showEndSessionDialog = false },
            title = { Text("Kết thúc phiên tư vấn", fontWeight = FontWeight.Bold) },
            text = { Text("Phiên sẽ chuyển sang chế độ đọc và cả hai bên sẽ không gửi thêm tin nhắn được nữa. Lịch sử chat vẫn được lưu lại.") },
            confirmButton = {
                Button(
                    onClick = {
                        showEndSessionDialog = false
                        val bookingId = state.thread?.bookingRequestId
                        if (bookingId != null) {
                            viewModel.completeConsultation(bookingId) {}
                        }
                    },
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

    // Restrict messaging dialog
    if (showRestrictDialog) {
        AlertDialog(
            onDismissRequest = { showRestrictDialog = false },
            title = { Text("Hạn chế nhắn tin", fontWeight = FontWeight.Bold) },
            text = { Text("Bệnh nhân sẽ không gửi tin nhắn được nữa trong phiên này. Bạn vẫn có thể kết thúc phiên sau đó.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestrictDialog = false
                        val bookingId = state.thread?.bookingRequestId
                        if (bookingId != null) {
                            viewModel.restrictMessaging(bookingId)
                        }
                    },
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
