package com.example.carenest.feature.community.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carenest.CareNestApplication
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.core.presentation.theme.PrimaryBlue
import com.example.carenest.feature.community.data.repository.CommunityRepository
import com.example.carenest.feature.community.domain.model.CreateGroupCreationRequest
import com.example.carenest.feature.community.domain.model.GroupCreationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CreateGroupRequestViewModel(
    private val repository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupRequestUiState())
    val uiState: StateFlow<CreateGroupRequestUiState> = _uiState.asStateFlow()

    init {
        refreshRequests()
    }

    fun refreshRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val requests = repository.getMyGroupRequests()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    requests = requests,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.userMessage("Không thể tải trạng thái yêu cầu")
                )
            }
        }
    }

    fun submitRequest(request: CreateGroupCreationRequest, onSubmitted: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null, message = null)
            try {
                repository.createGroupRequest(request)
                val requests = repository.getMyGroupRequests()
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    requests = requests,
                    message = "Yêu cầu đã được gửi và đang chờ quản trị viên duyệt."
                )
                onSubmitted()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.userMessage("Không thể gửi yêu cầu tạo nhóm")
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class CreateGroupRequestUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val requests: List<GroupCreationRequest> = emptyList(),
    val error: String? = null,
    val message: String? = null,
) {
    val latestRequest: GroupCreationRequest?
        get() = requests.firstOrNull()
}

class CreateGroupRequestViewModelFactory(
    private val repository: CommunityRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateGroupRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateGroupRequestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupRequestScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as CareNestApplication
    val viewModel: CreateGroupRequestViewModel = viewModel(
        factory = CreateGroupRequestViewModelFactory(app.communityRepository)
    )
    val state by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var shortDescription by remember { mutableStateOf("") }
    var detailedPurpose by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var groupType by remember { mutableStateOf("SPECIALTY_PUBLIC") }

    val latestRequest = state.latestRequest
    val hasPendingRequest = latestRequest?.status.equals("PENDING", ignoreCase = true)
    val canSubmit = !state.isSubmitting &&
        !hasPendingRequest &&
        name.isNotBlank() &&
        shortDescription.isNotBlank() &&
        detailedPurpose.isNotBlank() &&
        category.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yêu cầu tạo hội nhóm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshRequests) {
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
                .background(Color(0xFFF1F5F9))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Khởi tạo cộng đồng chuyên môn",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    "Gửi yêu cầu trước khi nhóm được mở để đảm bảo chất lượng nội dung và quản trị.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            when {
                state.isLoading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    }
                }

                latestRequest != null -> {
                    LatestRequestStatusCard(
                        request = latestRequest
                    )
                }
            }

            state.message?.let { message ->
                InfoBanner(
                    text = message,
                    backgroundColor = Color(0xFFDCFCE7),
                    textColor = Color(0xFF166534)
                )
            }

            state.error?.let { error ->
                InfoBanner(
                    text = error,
                    backgroundColor = Color(0xFFFEE2E2),
                    textColor = Color(0xFFB91C1C)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên hội nhóm") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = PrimaryBlue
                        )
                    )

                    OutlinedTextField(
                        value = shortDescription,
                        onValueChange = { shortDescription = it },
                        label = { Text("Mô tả ngắn") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = PrimaryBlue
                        )
                    )

                    OutlinedTextField(
                        value = detailedPurpose,
                        onValueChange = { detailedPurpose = it },
                        label = { Text("Mục đích hoạt động") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = PrimaryBlue
                        )
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Chuyên khoa hoặc chủ đề") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = PrimaryBlue
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Loại hội nhóm",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF334155)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GroupTypeOptionCard(
                                modifier = Modifier.weight(1f),
                                title = "Cộng đồng",
                                subtitle = "Thảo luận mở theo chuyên khoa",
                                selected = groupType == "SPECIALTY_PUBLIC",
                                onClick = { groupType = "SPECIALTY_PUBLIC" }
                            )
                            GroupTypeOptionCard(
                                modifier = Modifier.weight(1f),
                                title = "Phòng khám số",
                                subtitle = "Không gian tư vấn do bác sĩ quản lý",
                                selected = groupType == "DOCTOR_CLINIC",
                                onClick = { groupType = "DOCTOR_CLINIC" }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.submitRequest(
                        CreateGroupCreationRequest(
                            name = name.trim(),
                            shortDescription = shortDescription.trim(),
                            detailedPurpose = detailedPurpose.trim(),
                            category = category.trim(),
                            coverImageUrl = null,
                            groupType = groupType,
                            moderationIntent = "STRICT",
                            communityRules = null
                        )
                    ) {
                        name = ""
                        shortDescription = ""
                        detailedPurpose = ""
                        category = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = canSubmit
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        if (hasPendingRequest) "Đang chờ duyệt yêu cầu hiện tại" else "Gửi yêu cầu kiểm duyệt",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LatestRequestStatusCard(
    request: GroupCreationRequest,
) {
    val title: String
    val subtitle: String
    val badgeText: String
    val badgeColor: Color
    val badgeBg: Color
    when (request.status.uppercase()) {
        "PENDING" -> {
            title = "Bạn đang có yêu cầu chờ duyệt"
            subtitle = "Trong thời gian chờ duyệt, bạn chưa thể gửi thêm yêu cầu mới."
            badgeText = "Chờ duyệt"
            badgeColor = Color(0xFFD97706)
            badgeBg = Color(0xFFFEF3C7)
        }
        "APPROVED" -> {
            title = "Yêu cầu gần nhất đã được duyệt"
            subtitle = "Nhóm của bạn đã có thể xuất hiện trong hệ thống. Hãy quay lại cộng đồng để kiểm tra."
            badgeText = "Đã duyệt"
            badgeColor = Color(0xFF059669)
            badgeBg = Color(0xFFD1FAE5)
        }
        else -> {
            title = "Yêu cầu gần nhất đã bị từ chối"
            subtitle = request.rejectionReason ?: "Bạn có thể điều chỉnh nội dung và gửi lại yêu cầu mới."
            badgeText = "Từ chối"
            badgeColor = Color(0xFFDC2626)
            badgeBg = Color(0xFFFEE2E2)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 20.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusMetaChip(label = request.category)
                StatusMetaChip(label = if (request.groupType == "DOCTOR_CLINIC") "Phòng khám số" else "Cộng đồng")
                request.createdAt?.let { StatusMetaChip(label = formatRequestTime(it)) }
            }
        }
    }
}

@Composable
private fun StatusMetaChip(label: String) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = Color(0xFF475569),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoBanner(
    text: String,
    backgroundColor: Color,
    textColor: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun GroupTypeOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) PrimaryBlue else Color(0xFFE2E8F0)
    val backgroundColor = if (selected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
    val titleColor = if (selected) PrimaryBlue else Color(0xFF0F172A)
    val subtitleColor = if (selected) Color(0xFF1D4ED8) else Color(0xFF64748B)

    Surface(
        modifier = modifier
            .height(112.dp)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = subtitleColor
            )
        }
    }
}

private fun formatRequestTime(value: String): String {
    return try {
        val instant = Instant.parse(value)
        instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
    } catch (_: Exception) {
        value.take(16)
    }
}
