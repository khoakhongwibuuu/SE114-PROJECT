package com.example.carenest.feature.ekyc.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.ekyc.domain.model.DoctorSummary
import com.example.carenest.feature.ekyc.domain.model.DoctorVerificationResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVerificationScreen(
    viewModel: AdminVerificationViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Rejection dialog state
    var showRejectDialog by remember { mutableStateOf(false) }
    var targetRejectId by remember { mutableStateOf<Long?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    // Image Zoom dialog state
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    // Toast effects
    LaunchedEffect(state.message, state.error) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Duyệt hồ sơ Bác sĩ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        viewModel.loadPending()
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Chờ duyệt", fontWeight = FontWeight.Bold)
                            if (state.pendingList.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = state.pendingList.size.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        viewModel.loadDoctors()
                    },
                    text = { Text("Danh sách Bác sĩ", fontWeight = FontWeight.Bold) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    if (state.isLoadingPending && state.pendingList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    } else if (state.pendingList.isEmpty()) {
                        EmptyListPlaceholder("Không có hồ sơ nào đang chờ duyệt.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.pendingList, key = { it.id }) { item ->
                                PendingVerificationCard(
                                    item = item,
                                    onApprove = { viewModel.approveVerification(item.id) },
                                    onReject = {
                                        targetRejectId = item.id
                                        rejectReason = ""
                                        showRejectDialog = true
                                    },
                                    onImageClick = { zoomImageUrl = item.documentUrl }
                                )
                            }
                        }
                    }
                } else {
                    if (state.isLoadingDoctors && state.doctorList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    } else if (state.doctorList.isEmpty()) {
                        EmptyListPlaceholder("Chưa có bác sĩ nào trong hệ thống.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.doctorList, key = { it.id }) { item ->
                                DoctorSummaryCard(
                                    item = item,
                                    onRevoke = { viewModel.revokeDoctor(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection Reason Modal/Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Lý do từ chối", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Nhập lý do từ chối hồ sơ này. Bác sĩ sẽ nhìn thấy phản hồi này để sửa đổi.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Ví dụ: Ảnh chứng chỉ mờ, sai thông tin chuyên khoa...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        targetRejectId?.let { id ->
                            viewModel.rejectVerification(id, rejectReason)
                        }
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Từ chối hồ sơ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Hủy", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Zoom Image Dialog
    zoomImageUrl?.let { url ->
        AlertDialog(
            onDismissRequest = { zoomImageUrl = null },
            title = { Text("Ảnh chứng chỉ đính kèm", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Zoomed certificate",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { zoomImageUrl = null }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun PendingVerificationCard(
    item: DoctorVerificationResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onImageClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Doctor Name & Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.userFullName ?: "Bác sĩ ẩn danh",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        item.userEmail ?: "Chưa cập nhật email",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(16.dp))

            // Details
            DetailRow(label = "Chuyên khoa", value = item.specialty)
            DetailRow(label = "Bệnh viện", value = item.hospitalName)
            DetailRow(label = "Số chứng chỉ hành nghề", value = item.certificationNumber)

            Spacer(modifier = Modifier.height(12.dp))

            // Certificate Image Preview
            Text(
                "Ảnh chứng chỉ đính kèm:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable { onImageClick() }
            ) {
                AsyncImage(
                    model = item.documentUrl,
                    contentDescription = "Doctor Certificate",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color(0x99000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Phóng to", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Từ chối", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Phê duyệt", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DoctorSummaryCard(
    item: DoctorSummary,
    onRevoke: () -> Unit
) {
    var showConfirmRevoke by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.avatarUrl != null) {
                    AsyncImage(
                        model = item.avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.fullName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        item.email,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow(label = "Chuyên khoa", value = item.specialty ?: "N/A")
            DetailRow(label = "Bệnh viện", value = item.hospitalName ?: "N/A")
            DetailRow(label = "Chứng chỉ", value = item.certificationNumber ?: "N/A")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showConfirmRevoke = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2)),
                elevation = null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thu hồi quyền Bác sĩ", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    if (showConfirmRevoke) {
        AlertDialog(
            onDismissRequest = { showConfirmRevoke = false },
            title = { Text("Thu hồi quyền bác sĩ", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn thu hồi tư cách bác sĩ của ${item.fullName}? Họ sẽ trở thành người dùng bình thường.") },
            confirmButton = {
                Button(
                    onClick = {
                        onRevoke()
                        showConfirmRevoke = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Đồng ý thu hồi", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRevoke = false }) {
                    Text("Hủy", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyListPlaceholder(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFCBD5E1))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
