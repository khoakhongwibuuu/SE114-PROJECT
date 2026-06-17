package com.example.carenest.feature.admin.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.community.domain.model.GroupCreationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AdminGroupRequestsViewModel(
    private val repository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminGroupRequestsUiState())
    val uiState: StateFlow<AdminGroupRequestsUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
            try {
                val requests = repository.getAdminGroupRequests()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    requests = requests
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Không thể tải danh sách yêu cầu"
                )
            }
        }
    }

    fun approveRequest(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actingRequestId = id, error = null, message = null)
            try {
                repository.approveGroupRequest(id)
                val requests = repository.getAdminGroupRequests()
                _uiState.value = _uiState.value.copy(
                    actingRequestId = null,
                    requests = requests,
                    message = "Yêu cầu đã được duyệt."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    actingRequestId = null,
                    error = e.localizedMessage ?: "Không thể duyệt yêu cầu"
                )
            }
        }
    }

    fun rejectRequest(id: Long, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actingRequestId = id, error = null, message = null)
            try {
                repository.rejectGroupRequest(id, reason)
                val requests = repository.getAdminGroupRequests()
                _uiState.value = _uiState.value.copy(
                    actingRequestId = null,
                    requests = requests,
                    message = "Yêu cầu đã bị từ chối."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    actingRequestId = null,
                    error = e.localizedMessage ?: "Không thể từ chối yêu cầu"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AdminGroupRequestsUiState(
    val isLoading: Boolean = false,
    val requests: List<GroupCreationRequest> = emptyList(),
    val error: String? = null,
    val message: String? = null,
    val actingRequestId: Long? = null
)

class AdminGroupRequestsViewModelFactory(
    private val repository: CommunityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminGroupRequestsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminGroupRequestsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGroupRequestsScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as CareNestApplication
    val viewModel: AdminGroupRequestsViewModel = viewModel(
        factory = AdminGroupRequestsViewModelFactory(app.communityRepository)
    )
    val state by viewModel.uiState.collectAsState()
    var rejectTarget by remember { mutableStateOf<GroupCreationRequest?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý yêu cầu hội nhóm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadRequests) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
        ) {
            state.message?.let { message ->
                BannerCard(
                    text = message,
                    containerColor = Color(0xFFDCFCE7),
                    textColor = Color(0xFF166534),
                    onDismiss = viewModel::clearMessage
                )
            }

            state.error?.let { error ->
                BannerCard(
                    text = error,
                    containerColor = Color(0xFFFEE2E2),
                    textColor = Color(0xFFB91C1C),
                    onDismiss = viewModel::clearError
                )
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                state.requests.isEmpty() && !state.error.isNullOrBlank() -> {
                    EmptyRequestState(
                        title = "Không thể tải danh sách yêu cầu",
                        message = state.error!!,
                        onRetry = viewModel::loadRequests
                    )
                }

                state.requests.isEmpty() -> {
                    EmptyRequestState(
                        title = "Không có yêu cầu nào",
                        message = "Khi bác sĩ gửi yêu cầu tạo nhóm mới, danh sách sẽ xuất hiện ở đây.",
                        onRetry = viewModel::loadRequests
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.requests, key = { it.id }) { request ->
                            AdminGroupRequestCard(
                                request = request,
                                isActing = state.actingRequestId == request.id,
                                onApprove = { viewModel.approveRequest(request.id) },
                                onReject = {
                                    rejectTarget = request
                                    rejectReason = request.rejectionReason.orEmpty()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    rejectTarget?.let { request ->
        AlertDialog(
            onDismissRequest = {
                rejectTarget = null
                rejectReason = ""
            },
            title = { Text("Từ chối yêu cầu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Nhập lý do để bác sĩ có thể chỉnh sửa và gửi lại chính xác hơn.",
                        color = Color(0xFF475569),
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Lý do từ chối") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectRequest(request.id, rejectReason.trim())
                        rejectTarget = null
                        rejectReason = ""
                    },
                    enabled = rejectReason.isNotBlank() && state.actingRequestId == null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    rejectTarget = null
                    rejectReason = ""
                }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun BannerCard(
    text: String,
    containerColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = textColor,
                modifier = Modifier.weight(1f),
                lineHeight = 20.sp
            )
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = textColor)
            }
        }
    }
}

@Composable
private fun EmptyRequestState(
    title: String,
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = Color(0xFF64748B),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Làm mới", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun AdminGroupRequestCard(
    request: GroupCreationRequest,
    isActing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = request.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.shortDescription,
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                }

                val (statusColor, statusBg, statusText) = when (request.status) {
                    "PENDING" -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), "Chờ duyệt")
                    "APPROVED" -> Triple(Color(0xFF059669), Color(0xFFD1FAE5), "Đã duyệt")
                    "REJECTED" -> Triple(Color(0xFFDC2626), Color(0xFFFEE2E2), "Từ chối")
                    else -> Triple(Color.Gray, Color(0xFFF1F5F9), request.status)
                }
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            RequestMetaRow(
                label = "Loại nhóm",
                value = if (request.groupType == "SPECIALTY_PUBLIC") "Cộng đồng chuyên khoa" else "Phòng khám số"
            )
            RequestMetaRow(label = "Chuyên khoa", value = request.category)
            request.createdAt?.let { RequestMetaRow(label = "Gửi lúc", value = formatAdminRequestTime(it)) }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Mục đích hoạt động",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = request.detailedPurpose,
                fontSize = 14.sp,
                color = Color(0xFF334155),
                lineHeight = 20.sp
            )

            if (!request.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Lý do từ chối",
                    fontSize = 13.sp,
                    color = Color(0xFFB91C1C),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = request.rejectionReason,
                    fontSize = 14.sp,
                    color = Color(0xFF7F1D1D),
                    lineHeight = 20.sp
                )
            }

            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isActing,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Từ chối", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onApprove,
                        enabled = !isActing,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        if (isActing) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Duyệt ngay", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestMetaRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color(0xFF334155)
        )
    }
}

private fun formatAdminRequestTime(value: String): String {
    return try {
        val instant = Instant.parse(value)
        instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (_: Exception) {
        value.take(16)
    }
}
